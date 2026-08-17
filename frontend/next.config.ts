import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Proxy API requests to the Spring Boot backend during development.
  // In production, configure a reverse proxy (nginx/Caddy) instead.
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${process.env.API_BASE_URL || "http://localhost:8080"}/api/:path*`,
      },
    ];
  },
  // Disable x-powered-by header for security
  poweredByHeader: false,
};

export default nextConfig;
