# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 1.0.x   | Yes (current development) |

## Reporting a Vulnerability

**Do NOT open a public GitHub issue for security vulnerabilities.**

If you discover a security vulnerability in RegalPine DDD Framework, please report it responsibly:

### How to Report

1. **Email**: Send details to **regalpine-security@proton.me**
2. **Subject line**: `[RegalPine-DDD Security] <brief description>`
3. **Include**:
   - Description of the vulnerability
   - Steps to reproduce (proof-of-concept if possible)
   - Affected module(s): core / domain / application / cqrs / event / transaction / messaging / infrastructure / mybatis / jdbc / kafka / rabbitmq
   - Potential impact assessment
   - Suggested fix (optional but appreciated)

### Response Timeline

| Stage | Timeframe |
|-------|-----------|
| Acknowledgment of receipt | Within 48 hours |
| Initial triage & severity assessment | Within 7 days |
| Status update with remediation plan | Within 14 days |
| Fix released (critical/high) | Within 30 days |
| Public disclosure coordination | Mutually agreed timeline |

### What to Expect

- We will confirm receipt and begin investigation promptly
- We will keep you informed of progress
- We will credit you in the security advisory (unless you prefer anonymity)
- We will coordinate disclosure timing with you
- We will publish a GitHub Security Advisory (GHSA) for confirmed vulnerabilities

## Scope

The following are in scope:

- Core domain model serialization and deserialization (`ddd-core`)
- Aggregate invariant enforcement and state mutation (`ddd-core`, `ddd-domain`)
- Command / Query authorization bypass (`ddd-application`, `ddd-cqrs`)
- Outbox / Inbox message integrity and deduplication (`ddd-event`)
- Transaction isolation and Unit of Work consistency (`ddd-transaction`)
- Message serialization and broker authentication (`ddd-messaging`, `ddd-messaging-kafka`, `ddd-messaging-rabbitmq`)
- SQL injection via query AST (`ddd-infrastructure-mybatis`)
- JDBC connection and credential handling (`ddd-infrastructure-jdbc`)
- Spring Boot auto-configuration property injection (`ddd-spring-boot`)

## Out of Scope

- Denial-of-service via resource exhaustion (unless it bypasses rate limits)
- Social engineering attacks
- Vulnerabilities in third-party dependencies (report upstream: MyBatis, Spring, etc.)
- Issues in transitive dependencies managed by Maven BOMs
- Example application configurations (`examples/`)

## Security Best Practices for Deployments

- Always validate and sanitize aggregate command inputs
- Enable outbox pattern for reliable event publishing in production
- Use parameterized queries via the MyBatis AST query wrapper; never concatenate raw SQL
- Restrict messaging broker credentials and network access
- Keep dependencies updated (`mvn versions:display-dependency-updates`)
- Monitor Spring Boot Actuator endpoints and restrict exposure
- Run conformance tests (`ddd-conformance`) after upgrading to verify adapter compliance
