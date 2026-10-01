<div align="center">

# ShutterShot

**A photographer-booking marketplace for Bangladesh**

Clients browse verified local photographers by district and style, check
real availability, book a session, and pay a deposit online. Photographers
manage their own portfolio, packages, calendar, and bookings — and reply to
the feedback clients leave.

[![Java](https://img.shields.io/badge/Java-17-C15A3A?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-C15A3A?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-C15A3A?style=flat-square&logo=react&logoColor=white)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8-C15A3A?style=flat-square&logo=vite&logoColor=white)](https://vite.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-v4-C15A3A?style=flat-square&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-C15A3A?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)

</div>

---

## Contents

- [Features](#features)
  - [For clients](#for-clients)
  - [For photographers](#for-photographers)
  - [For admins and moderators](#for-admins-and-moderators)
- [Tech stack](#tech-stack)
- [Project layout](#project-layout)
- [Getting started](#getting-started)

## Features

### For clients

- Search photographers by name, district, and style — category matches are
  checked against a photographer's own verified, published portfolio work,
  not a self-reported label
- Browse portfolios, packages, reviews, and a live availability calendar
  before reaching out
- Book a session, verified however's convenient: phone OTP, email OTP, a QR
  code scanned in person, or a Google Authenticator (TOTP) code
- Pay the booking deposit online through SSLCommerz, with payment confirmed
  server-to-server before anything counts as paid
- Rate a completed session with a star rating and comment — published on the
  photographer's public profile immediately
- Shop photography accessories — lenses, lighting, tripods, and more — in
  the built-in Marketplace, with guest checkout, cash on delivery or online
  payment
- Browse wedding and event music suggestions and a curated pose gallery for
  inspiration before a shoot
- Ask questions about photographers, pricing, and availability through a
  full-page AI chat, a spoken voice assistant, or a floating chat widget
  available from anywhere on the site
- An Entertainment page with 17 small games for a wait between bookings
- Request account deletion at any time — an admin reviews every request
  before anything is removed

### For photographers

- Manage a portfolio, packages, availability calendar, and incoming bookings
  from a dedicated dashboard
- Every portfolio upload is screened automatically before it goes live: a
  local AI-detection model checks whether the image is AI-generated, and a
  perceptual-hash check catches photos already published elsewhere on the
  site — either rejects the upload immediately, with the reason shown to the
  photographer, but every photo (published or not) stays reviewable from the
  admin dashboard
- Reply publicly to the feedback clients leave, from a dedicated Feedback
  section — ratings publish on their own and count toward the public average
  immediately; photographers can reply but can never remove them
- Buy gear from the Marketplace using the same account
- An optional paid "Verified" blue badge

### For admins and moderators

- Approve photographer accounts and moderate portfolio photos — including
  re-running the AI check by hand — and remove abusive reviews (the only way
  a published review comes down)
- Run the Marketplace side of the business: manage the product catalogue and
  the order pipeline, from placed to delivered
- Manage the pose gallery, music suggestions, and photoshoot categories
  shown to clients
- Review account-deletion requests submitted by clients or photographers,
  and approve or decline each one
- Manage staff accounts and blue-badge pricing (restricted to the main
  admin)
- Remove accounts and browse a removal history log
- A dashboard with booking and revenue charts
- Light and dark theme, matched across the whole site

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.3.4, Spring Security (JWT auth), Spring Data JPA |
| Database | PostgreSQL |
| Frontend | React 19, Vite, Tailwind CSS v4, React Router |
| Payments | SSLCommerz |
| AI | A local ONNX model for portfolio AI-image screening; Groq and Gemini for chat/voice |

## Project layout

```
shuttershot-backend/    Spring Boot REST API
shuttershot-frontend/   React single-page app
```

## Getting started

See [SETUP.md](SETUP.md) for local setup — starting Postgres, configuring
environment variables, and running the backend and frontend.
