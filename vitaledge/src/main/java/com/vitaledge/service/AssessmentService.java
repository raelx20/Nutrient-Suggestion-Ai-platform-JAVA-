package com.vitaledge.service;

import com.vitaledge.common.exception.ApiException;
import com.vitaledge.common.exception.ForbiddenException;
import com.vitaledge.config.ApplicationProperties;
import com.vitaledge.domain.assessment.AssessmentAnswer;
import com.vitaledge.domain.assessment.AssessmentSession;
import com.vitaledge.domain.assessment.AssessmentStatus;
import com.vitaledge.domain.assessment.HealthProfile;
import com.vitaledge.domain.assessment.Question;
import com.vitaledge.domain.assessment.Questionnaire;
import com.vitaledge.domain.recommendation.Recommendation;
import com.vitaledge.domain.recommendation.RecommendationScoreHistory;
import com.vitaledge.domain.user.User;
import com.vitaledge.repository.AssessmentAnswerRepository;
import com.vitaledge.repository.AssessmentSessionRepository;
import com.vitaledge.repository.HealthProfileRepository;
import com.vitaledge.repository.QuestionRepository;
import com.vitaledge.repository.QuestionnaireRepository;
import com.vitaledge.repository.RecommendationRepository;
import com.vitaledge.repository.RecommendationScoreHistoryRepository;
import com.vitaledge.repository.UserRepository;
import com.vitaledge.web.dto.assessment.AnswerRequest;
import com.vitaledge.web.dto.assessment.AnswerResponse;
import com.vitaledge.web.dto.assessment.AssessmentDetailResponse;
import com.vitaledge.web.dto.assessment.AssessmentSummary;
import com.vitaledge.web.dto.assessment.QuestionResponse;
import com.vitaledge.web.dto.assessment.RecommendationResponse;
import com.vitaledge.web.dto.assessment.StartAssessmentResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Assessment lifecycle: start, answer, complete (build health profile + generate
 * recommendations) and retrieval. Ports the source project's {@code api/assessment.py}
 * endpoints including their per-operation rate limits and the abandonment of stale sessions.
 */
@Service
public class AssessmentService {

    private final AssessmentSessionRepository sessionRepository;
    private final AssessmentAnswerRepository answerRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final HealthProfileService healthProfileService;
    private final RecommendationService recommendationService;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationScoreHistoryRepository scoreHistoryRepository;
    private final UserRepository userRepository;
    private final RateLimitService rateLimitService;
    private final ApplicationProperties properties;

    public AssessmentService(AssessmentSessionRepository sessionRepository,
                             AssessmentAnswerRepository answerRepository,
                             QuestionnaireRepository questionnaireRepository,
                             QuestionRepository questionRepository,
                             HealthProfileRepository healthProfileRepository,
                             HealthProfileService healthProfileService,
                             RecommendationService recommendationService,
                             RecommendationRepository recommendationRepository,
                             RecommendationScoreHistoryRepository scoreHistoryRepository,
                             UserRepository userRepository,
                             RateLimitService rateLimitService,
                             ApplicationProperties properties) {
        this.sessionRepository = sessionRepository;
        this.answerRepository = answerRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.questionRepository = questionRepository;
        this.healthProfileRepository = healthProfileRepository;
        this.healthProfileService = healthProfileService;
        this.recommendationService = recommendationService;
        this.recommendationRepository = recommendationRepository;
        this.scoreHistoryRepository = scoreHistoryRepository;
        this.userRepository = userRepository;
        this.rateLimitService = rateLimitService;
        this.properties = properties;
    }

    @Transactional
    public StartAssessmentResponse start(UUID userId) {
        rateLimitService.check("assessment:create", userId.toString(),
                properties.getRateLimit().getAssessmentCreatePerHour(), 3600);

        User user = requireUser(userId);

        sessionRepository.abandonIncompleteForUser(userId, AssessmentStatus.abandoned,
                AssessmentStatus.in_progress, LocalDateTime.now());

        Questionnaire questionnaire = questionnaireRepository.findByDefaultQuestionnaireTrueAndActiveTrue()
                .orElseThrow(() -> new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                        "QUESTIONNAIRE_UNAVAILABLE", "No active questionnaire is available."));

        AssessmentSession session = new AssessmentSession(user, questionnaire);
        sessionRepository.save(session);

        List<Question> questions = questionRepository
                .findByQuestionnaireIdAndActiveTrueOrderByOrderIndexAsc(questionnaire.getId());
        List<QuestionResponse> questionDtos = questions.stream()
                .map(q -> new QuestionResponse(q.getId(), q.getKey(), q.getText(), q.getType(),
                        q.getOptions(), q.isRequired(), q.getOrderIndex()))
                .toList();

        return StartAssessmentResponse.of(session, questionDtos, 0);
    }

    @Transactional
    public AnswerResponse.BatchResult submitAnswers(UUID userId, UUID assessmentId,
                                                    List<AnswerRequest> requests) {
        rateLimitService.check("assessment:answer", userId.toString(),
                properties.getRateLimit().getAssessmentAnswerPerHour(), 3600);

        AssessmentSession session = requireActiveSession(userId, assessmentId);

        List<AnswerResponse> saved = new ArrayList<>();
        for (AnswerRequest request : requests) {
            Question question = questionRepository.findByIdAndQuestionnaireId(
                            request.questionId(), session.getQuestionnaire().getId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "QUESTION_NOT_FOUND",
                            "Question does not belong to this assessment."));

            Optional<AssessmentAnswer> existing = answerRepository
                    .findByAssessmentIdAndQuestionId(assessmentId, question.getId());
            AssessmentAnswer answer = existing.orElseGet(() ->
                    new AssessmentAnswer(session, question, request.value()));
            if (!existing.isPresent()) {
                answerRepository.save(answer);
            } else {
                answer.setAnswerValue(request.value());
                answerRepository.save(answer);
            }
            saved.add(AnswerResponse.from(answer));
        }

        int answered = (int) answerRepository.countByAssessmentId(assessmentId);
        AssessmentSummary summary = AssessmentSummary.of(session, null, answered);
        return new AnswerResponse.BatchResult(saved, summary);
    }

    @Transactional
    public AssessmentDetailResponse complete(UUID userId, UUID assessmentId) {
        rateLimitService.check("assessment:complete", userId.toString(),
                properties.getRateLimit().getAssessmentCompletePerHour(), 3600);

        AssessmentSession session = requireActiveSession(userId, assessmentId);
        if (session.getStatus() != AssessmentStatus.in_progress) {
            throw new ApiException(HttpStatus.CONFLICT, "ASSESSMENT_NOT_IN_PROGRESS",
                    "Assessment is not in progress.");
        }

        List<AssessmentAnswer> answers = answerRepository.findByAssessmentId(assessmentId);
        if (answers.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NO_ANSWERS",
                    "Cannot complete an assessment with no answers.");
        }

        User user = session.getUser();
        HealthProfile healthProfile = healthProfileService.buildProfile(user, session, answers);

        List<Recommendation> recommendations =
                recommendationService.generate(user, session, healthProfile);

        session.markCompleted();
        sessionRepository.save(session);

        List<RecommendationResponse> recoDtos = recommendations.stream()
                .map(this::toResponse)
                .toList();

        return AssessmentDetailResponse.of(session, healthProfile, recoDtos);
    }

    @Transactional(readOnly = true)
    public AssessmentDetailResponse get(UUID userId, UUID assessmentId) {
        AssessmentSession session = requireOwnedSession(userId, assessmentId);
        Optional<HealthProfile> hp = healthProfileRepository.findByAssessmentIdAndUserId(assessmentId, userId);
        List<Recommendation> recommendations =
                recommendationRepository.findByAssessmentIdAndUserIdOrderByRankAsc(assessmentId, userId);
        List<RecommendationResponse> recoDtos = recommendations.stream()
                .map(this::toResponse)
                .toList();
        return AssessmentDetailResponse.of(session, hp.orElse(null), recoDtos);
    }

    private RecommendationResponse toResponse(Recommendation reco) {
        List<RecommendationScoreHistory> history = scoreHistoryRepository.findByRecommendationId(reco.getId());
        List<RecommendationResponse.ScoreComponent> components = history.stream()
                .map(h -> new RecommendationResponse.ScoreComponent(h.getMetric(), h.getValue()))
                .toList();
        return new RecommendationResponse(
                reco.getId(),
                reco.getProduct().getId(),
                reco.getProduct().getName(),
                reco.getProduct().getSku(),
                reco.getProduct().getCategory().name(),
                reco.getProduct().getCategoryCode(),
                reco.getProduct().getAgeGroup().name(),
                reco.getReason(),
                reco.getConfidence(),
                reco.getRank(),
                reco.isSafe(),
                reco.isExcluded(),
                components);
    }

    private AssessmentSession requireActiveSession(UUID userId, UUID assessmentId) {
        return sessionRepository.findByIdAndUserId(assessmentId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ASSESSMENT_NOT_FOUND",
                        "Assessment not found for this user."));
    }

    private AssessmentSession requireOwnedSession(UUID userId, UUID assessmentId) {
        return requireActiveSession(userId, assessmentId);
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ForbiddenException("User not found."));
    }
}