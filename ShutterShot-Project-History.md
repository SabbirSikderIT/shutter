# ShutterShot — Project History and Technical Specification

**What this file is.** A complete handoff document for a full-stack web application called ShutterShot. It is written so that an AI coding assistant (or a developer) who has never seen the project can understand exactly what was built, how, and why — and can build a project of the same scope and quality, either a faithful rebuild or a similar product in a different niche.

**Snapshot:** built between 31 Aug and 2 Oct 2026 across 46 commits. About 220 backend Java files and 89 frontend source files. 58 automated backend tests.

---

## 0. How to use this file

**If you are an AI assistant reading this:** treat every section as specification and as the working style to follow, not as background. The person who gave you this file has the finished product and no memory of it in your session. If something here conflicts with what they tell you in the conversation, the conversation wins.

**Suggested prompt to give together with this file:**

> Read the attached project document fully before writing any code. Build a new project of the same scope and quality, for this domain: *[describe your domain]*. Follow section 13 (build order) and section 14 (working style). Build one feature at a time, verify each one for real before moving on (compile, run tests, and exercise it in a running app and browser), and tell me plainly what you tested and what you could not test. Do not copy the known weaknesses listed in section 12.

**Reading guide.** Sections 1–4 explain what and why. Sections 5–9 are the precise specification (data, features, API, UI). Sections 10–12 are verification, lessons learned, and known weaknesses. Sections 13–14 are how to build and how to work.

---

## 1. Product summary

ShutterShot is a two-sided marketplace web app for Bangladesh. Clients find and book verified local photographers; photographers run their business on the platform; admins keep it trustworthy. A small accessories shop and several AI features sit alongside.

| User | What they do |
|---|---|
| **Customer / guest** | Search photographers by name, district and style. View portfolios, packages, reviews and a live availability calendar. Book a session (no account needed), verify the booking, pay a deposit online, and rate the session afterwards. Shop for camera accessories. Chat with an AI assistant by text or voice. Request deletion of their own account. |
| **Photographer** | Dashboard with charts, portfolio manager (uploads are screened by AI), package manager, availability calendar, incoming booking requests, feedback/replies, profile settings, optional paid "verified" blue badge. Can also shop and request account deletion. |
| **Admin / moderator** | Moderate reviews and portfolio photos, manage users (verify, delete, ban), manage content (poses, music, photoshoot categories), run the shop (products, orders), review account-deletion requests, see analytics. The **main admin** alone additionally manages staff accounts, blue-badge pricing, account history views, and the booking deposit percentage. |

Money and trust are central: deposits and shop orders are paid through SSLCommerz (a Bangladeshi payment gateway), and every price, stock level and payment confirmation is verified on the server.

---

## 2. Build history

The project was built iteratively, one reviewable feature per commit, by a small team plus an AI coding assistant, with the owner reviewing visually and steering every step. Rough phases:

| Dates (2026) | Phase | What was added |
|---|---|---|
| 31 Aug – 3 Sep | **Foundation** | Initial repo. Spring Boot backend: auth, photographer profiles, portfolio uploads, package CRUD, availability calendar, guest booking with OTP, reviews with admin approval, photo moderation and user management. React frontend scaffold, design-system reference doc, Home and Search pages, photographer profile, booking flow, dashboard pages (portfolio, packages, calendar, booking requests), first admin panel, `SETUP.md`. |
| 4 – 14 Sep | **Consolidation** | Frontend and backend merged into one repository. |
| 15 – 17 Sep | **Accounts and admin depth** | Admin portal, password reset by email, Home redesign, FAQ page, portfolio moderation, profile settings, dashboard charts, first AI chatbot, customer accounts (bookings linked to logged-in users), expanded admin panel, staff management restricted to the main admin. |
| 19 – 21 Sep | **Payments and verification** | District-based search, account-removal options, SSLCommerz payments (booking deposit and blue badge), four booking-verification methods, AI photo check, Entertainment games. |
| 22 – 23 Sep | **AI screening, polish, voice** | Automatic upload screening (local ONNX model plus duplicate detection), dark mode, redesigned header, full-page typed and spoken AI chat, photographer-approved ratings, docs refresh. |
| 25 Sep | **Content and commerce** | Pose gallery, direct-publish reviews, paged search, music suggestions, admin management of music and categories, **Accessories Marketplace** with cart, checkout and SSLCommerz payment. |
| 28 Sep – 2 Oct | **Self-service, hardening, configurability** | Account-deletion requests with admin review; Detectra reject threshold lowered to 0.50; category-search fix; TOTP QR fix; navbar responsive rework; LAN dev access for phone QR scanning; admin-configurable booking deposit percentage ("Booking Money"). |

The ordering matters: auth and roles first, then the core listing and booking transaction, then one payment method used as a reusable pattern, then provider-side tools, then admin, then secondary features (shop, AI, content).

---

## 3. Stack, repository layout, running it

| Layer | Choice | Notes |
|---|---|---|
| Backend language | Java 17 | Spring Boot 3.3.4 needs 17+; the machine's default JDK may be newer, so point `JAVA_HOME` at a 17 install when building |
| Backend framework | Spring Boot 3.3.4 | Web, Data JPA, Security, Validation, Mail starters |
| Database | PostgreSQL 16 | Docker Compose, host port **5433**; Hibernate `ddl-auto: update` manages schema |
| Auth | JWT | Stateless; jjwt 0.12.6; role in the token; per-endpoint rules in `SecurityConfig` |
| Build | Maven Wrapper | `./mvnw` / `mvnw.cmd`, no local Maven needed |
| Frontend | React 19, Vite 8, Tailwind CSS v4, React Router 7, axios | No TypeScript, no ESLint, no frontend test runner |
| State | React Context | Auth, cart, theme |
| Payments | SSLCommerz | Sandbox by default |
| AI | Groq (chat, Whisper speech-to-text), Google Gemini (widget), local ONNX model "Detectra v3" (image screening), optional AI-or-Not API (admin deep check) | All optional; features degrade gracefully without keys |
| Other libs | ZXing (QR images), `googleauth` (TOTP), metadata-extractor (EXIF), ONNX Runtime | |

```
shutter/
  README.md                  product overview
  SETUP.md                   local setup and env var table
  shuttershot-backend/       Spring Boot REST API (Maven)
    docker-compose.yml       Postgres 16 container
    models/detectra-v3/      model.onnx (local AI-image detector)
    src/main/java/com/shuttershot/
      config/ controller/ dto/ exception/ model/ repository/ service/ util/
    src/main/resources/      application.yml, product-images/ (bundled sample photos)
    src/test/java/...        7 test classes
  shuttershot-frontend/      React single-page app (Vite)
    src/pages/ src/pages/AdminPanel/ src/components/ src/context/ src/services/api.js src/utils/ src/styles/index.css
    public/                  camera/, showcase/ (hero + story images)
```

**Run locally:** `docker compose up -d` (Postgres) → `./mvnw spring-boot:run` (API on :8080) → `npm install && npm run dev` (app on :5173). Defaults are chosen so a fresh clone runs with zero configuration. Backend and frontend are independent projects talking over REST.

**Phone testing on the same Wi-Fi** (needed to scan booking QR codes): Vite is bound to all interfaces (`server.host: true`), the backend allows private-LAN origins in CORS, and `APP_BASE_URL` / `APP_FRONTEND_URL` plus the frontend's `VITE_API_BASE_URL` must use the machine's LAN IP instead of `localhost` (QR codes encode a URL a phone must be able to reach).

---

## 4. Architecture and conventions

### 4.1 Backend

- **Layering:** Controller (HTTP only) → Service (business rules; `@Transactional` wherever money, stock or multi-row state changes) → Repository (Spring Data JPA) → PostgreSQL.
- **DTOs at every API boundary.** Entities are never returned from controllers.
- **Constructor injection** (Lombok `@RequiredArgsConstructor`), never field injection. Entities use `@Builder/@Getter/@Setter`.
- **Enums model state** (`BookingStatus`, `OrderStatus`, `PaymentStatus`, …), sometimes with a little behaviour (`OrderStatus.isFinal()`), instead of scattered strings and booleans.
- **Errors:** custom exceptions mapped by one `@RestControllerAdvice` to `{timestamp, status, message}` JSON: duplicate 409, not found 404, bad credentials 401, access denied / disabled 403, invalid request or validation 400, everything unexpected 500. The frontend always reads `err.response.data.message`.
- **Validation** with Jakarta annotations on request DTOs; money-affecting inputs additionally re-checked in the service.
- **Startup runners** (`@Order`) seed reference data (products, built-in photoshoot categories) idempotently and perform one-off schema fixes; a `@Scheduled` job cleans up abandoned payments.
- **Singleton settings rows** (id always 1) hold admin-configurable values (`BlueBadgeSettings`, `BookingMoneySettings`). Reads never write: no row simply means "use the default".
- **"Main admin" concept:** one pre-existing account (`admin@shuttershot.com`) is the only one allowed to manage staff, blue-badge price and holders, account-history views, and the booking deposit percentage. The URL-level rule lets any ADMIN reach those endpoints; the **service** then throws unless the caller is the main admin (`MainAdminGuard`). The main admin account must be inserted by hand (BCrypt hash, role `ADMIN`) — nothing seeds it.
- **File storage:** uploads saved as `{UUID}{ext}` under one directory, served at `/uploads/**`, URLs stored absolute (`app.base-url`).

### 4.2 Frontend

- One file per page under `src/pages`; shared building blocks under `src/components`; Context for cross-cutting state; **one `services/api.js`** holding every API call (so the whole backend surface the UI depends on is visible in one file).
- API function convention: `export function fooBar(args) { return api.verb(path, body).then(res => res.data) }`. Optional params are passed as `value || undefined`. Uploads use `FormData` with no manual `Content-Type`.
- Every page that loads data has explicit **loading / error / empty / ready** branches, plain-language messages, and "don't flash" behaviour (e.g. optional widgets fail quietly).
- Lazy-loaded routes (everything except Home, SearchResults, NotFound).
- **Token handling:** the whole auth response is stored in `localStorage['shuttershot_auth']`; an axios request interceptor adds `Authorization: Bearer`.
- Destructive actions use `window.confirm` (or a password modal for the most sensitive ones); successful mutations update list state in place (filter out / replace) rather than refetching.

---

## 5. Data model

All ids are `Long` identity unless noted. Roles: `PHOTOGRAPHER, CUSTOMER, ADMIN, MODERATOR`.

**Identity and profiles**
- **User** — role, name, email (unique), passwordHash (BCrypt), phone, location, profilePhotoUrl, bio, verified, enabled, createdAt. (Also `storedPassword` — see §12.)
- **PhotographerProfile** — 1:1 with User; baseLocation (the district used by search), yearsExperience, ratingAvg, totalReviews, hasBlueBadge (denormalised so the hot search query can sort on it), specialties (legacy, unused).
- **BannedEmail**, **PasswordResetToken** (token, user, expiresAt, used), **OtpVerification** (contact, code, expiresAt, verified).
- **AccountDeletionRequest** — 1:1 with User (unique), reason, status (`PENDING`, `REJECTED`), adminNote, createdAt, resolvedAt. Approving deletes the request and the user in one transaction; a rejected user can resubmit (the row is reused).

**Service offering and scheduling**
- **Package** — photographer, title, description, price (10,2), durationHours, deliveryDays.
- **Availability** — photographer, date, status (`FREE, BOOKED, BLOCKED`), unique on (photographer, date).
- **Booking** — photographer, servicePackage, nullable customer user (guest bookings have none), clientName/phone/email, bookingDate, timeSlot (free text), status (`PENDING, CONFIRMED, COMPLETED, CANCELLED`), otpVerified (name predates multi-method verification), verificationMethod (`PHONE_OTP, EMAIL_OTP, QR_CODE, TOTP`), qrVerificationToken (unique), totpSecret, **depositAmount** (10,2), **depositPercent** (5,2, column default 10), depositPaid, createdAt. The deposit amount and percent are **snapshotted at creation**.
- **BookingPaymentTransaction** — tranId (unique), booking, amount, currency, status (`PENDING, VALID, FAILED, CANCELLED`), createdAt, validatedAt.
- **BookingMoneySettings** — singleton: depositPercent (5,2), updatedAt.
- **Review** — 1:1 with Booking (unique), photographer, clientName, rating 1–5, comment, status (default `APPROVED`; `REJECTED` only when an admin removes it), photographerReply, repliedAt.

**Portfolio**
- **PortfolioImage** — photographer, imageUrl, category (`PORTRAIT, WEDDING, LANDSCAPE, EVENT`), caption, exifData (JSON text), verificationStatus (`PENDING, VERIFIED, FLAGGED`), flagReason (admin-facing), rejectionReason (photographer-facing), contentHash (SHA-256), perceptualHash (64-bit), detectraVerdict/Confidence/CheckedAt (local model), aiCheck* (admin deep check).

**Blue badge**
- **BlueBadge** (1:1 photographer, amountPaid, purchasedAt, active), **BlueBadgeSettings** (singleton price, default 500.00), **BlueBadgeTransaction** (same shape as booking transactions).

**Shop**
- **Product** — name, description, category (`ESSENTIALS, LIGHTING, LENSES, SUPPORT, STORAGE, AUDIO_VIDEO, SOFTWARE, EVERYDAY`), price (integer BDT), oldPrice, stock, digital, iconKey, active, imageUrl, imageCredit, imageSource, sampleImageApplied.
- **ShopOrder** — orderNumber (`SS-yyyyMMdd-%06d`, unique), customer name/phone/email, district, address, note, paymentMethod (`CASH_ON_DELIVERY, SSLCOMMERZ`), paymentStatus (`UNPAID, PENDING, PAID, FAILED, CANCELLED`), tranId, paidAt, status (`PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED`), subtotal, deliveryFee, total, items (cascade).
- **ShopOrderItem** — snapshots: productId (plain Long), productName, unitPrice, quantity, lineTotal, digital.

**Content**
- **MusicSuggestion** (category `BANGLA, ENGLISH, HINDI, EVENT`; title, credit, 11-char YouTube videoId; unique per category+videoId), **PoseExample** (subsection `WEDDING, PORTRAIT, AESTHETIC, COUPLE, FAMILY`, imageUrl), **PhotoshootCategory** (name, summary, details, goodFor list, lookFor, searchValue — non-null marks the four built-ins which cannot be deleted).
- **AiChatConversation** (user, title, mode `text|voice`) and **AiChatMessage** (role, content).

---

## 6. Feature specifications

### 6.1 Authentication, roles, account security
- Register as photographer (district required, must be one of the 64) or customer; log in (JWT: subject = email, claims `userId`, `role`; default 24 h); separate admin login page that rejects non-staff roles.
- Forgot-password: 6-digit OTP by email → verify OTP → 30-minute reset token → set new password. The response is identical whether or not the email exists.
- Banned emails cannot re-register. Disabled accounts cannot authenticate.
- The JWT filter loads the user per request; any failure leaves the request anonymous.
- A standing rule of the project owner: **the admin account's password is never changed without their explicit, in-the-moment permission.**

### 6.2 Discovery and search
- Search by name/email/phone (case-insensitive substring), **district** (exact match on `baseLocation`, a fixed list of 64 grouped by division), and **category** (wedding, portrait, event, landscape).
- Category is matched against the photographer's **verified, published portfolio photos** (an EXISTS subquery), not a self-declared field. Unknown categories match nothing rather than erroring.
- Always restricted to users whose role is `PHOTOGRAPHER`. Sorted blue-badge holders first, then by id (stable paging). Paged (`page`, `size` default 12, max 50) with `{items, page, size, total, hasMore}`; the UI auto-loads the next page near the end of the list and also offers a manual button. A request-id guard stops stale responses overwriting newer results.
- Photographer profile page: portfolio (verified only), packages, approved reviews (6 at a time), read-only availability calendar (green free / red booked).

### 6.3 Booking flow (public, no account required)
1. **Details:** choose package, an available date (calendar) and a time slot (five two-hour slots), enter name/phone/email (prefilled for logged-in customers). Server checks: package belongs to photographer, date not in the past, availability row absent or FREE. The booking is created `PENDING` and the date is marked `BOOKED` immediately.
2. **Verify**, one of four methods (switchable until verified, which clears the previous method's secret):
   - `PHONE_OTP` — SMS via Textbelt (numbers normalised to `+880…`); the code is also returned in the response because the free SMS tier cannot reach Bangladesh.
   - `EMAIL_OTP` — SMTP email.
   - `QR_CODE` — a random token; the **photographer** opens the booking in their dashboard and shows a QR encoding `{frontend}/verify-booking/{token}`; the client scans it with their phone; the frontend page calls the verify endpoint. The booking page polls until verified.
   - `TOTP` — Google-Authenticator-compatible secret; the QR must encode the **raw `otpauth://totp/…` URI**; the client enters the 6-digit code.
3. **Deposit:** redirect to SSLCommerz to pay `package price × deposit% / 100` (see 6.4).
4. **Confirmed:** summary page with status labels; rating form once complete.
- **Status machine:** `PENDING → CONFIRMED | CANCELLED`; `CONFIRMED → COMPLETED | CANCELLED`; terminal states stay terminal. Only the owning photographer changes status. `CONFIRMED` requires verification **and** a paid deposit. `CANCELLED` frees the date again.
- After a gateway redirect the page reads `?payment=success|failed|cancelled&bookingId=…`, reloads the booking **from the server**, jumps to the right step, shows a banner for failure/cancel, then strips the query parameters.

### 6.4 The payment pattern (reused for deposits, blue badge, and shop orders)
This is the most reusable piece of the project.
1. **Reserve first, then redirect.** The booking/order is created and the scarce thing (slot or stock) reserved, marked `PENDING`, with a unique `tranId`, before the customer reaches the hosted payment page.
2. **Never trust the redirect.** SSLCommerz posts back to success/fail/cancel URLs as plain browser form posts with no JWT. These endpoints are public and are treated only as hints.
3. **Confirm server-to-server.** On "success" the backend calls SSLCommerz's validation API itself and marks paid only if the status is `VALID/VALIDATED` and the amount matches (within 0.01).
4. **Release on non-payment.** Fail/cancel releases the reservation (stock back, slot free). A scheduled job cancels online orders still `PENDING` after 60 minutes.
5. **Late payments.** If a valid payment arrives for an already-released order, the system tries to re-reserve atomically; if that is no longer possible it keeps the record, marks it paid, and flags it for manual refund instead of silently losing money or stock.
6. **CORS:** the three callback path groups need their own permissive CORS registration (`*`, GET/POST, no credentials) **registered before** the general same-origin rule, because the first matching pattern wins. This is the most common bug in the feature.
7. Unconfigured gateway (no store id/password): the shop reports online payment unavailable and refuses such orders. Booking-deposit and badge initiation simply call the gateway, which rejects the session; that surfaces as a server error (this path was not traced in detail — a rebuild should check for configuration up front and return a clear message instead).

### 6.5 Booking Money (admin-configurable deposit)
- Section **Booking Money** in the admin sidebar (main admin only). The main admin sets the percentage (1–100, up to 2 decimals). Default 10.
- Applies to **new** bookings only: amount **and** percent are stored on each booking at creation, so existing bookings keep what they were quoted and the UI labels ("Deposit (12.5%)") stay accurate per booking. Existing rows were backfilled to 10 via a column default.
- Amount = `price × percent ÷ 100`, one rounding step, HALF_UP to 2 decimals.
- The page shows a live example ("on a ৳10,000 package the client pays …").

### 6.6 Reviews
- A client reviews a **completed** booking, once per booking. Published immediately (`APPROVED`) — no pre-moderation. Photographers may reply (and edit the reply) but can never remove a review; only an admin can (`REJECTED`). `ratingAvg`/`totalReviews` are recomputed from approved reviews on create and on admin removal.

### 6.7 Photographer dashboard
Overview (monthly bookings and earnings bar charts; earned = confirmed or completed), Portfolio, Calendar (toggle a date FREE/BLOCKED; cannot change past or booked dates), Packages (CRUD), Bookings (grouped by status; accept/reject/complete; QR modal for QR verification), Feedback (reply to reviews), Profile settings, Verified Badge, Delete Account. The layout polls every 20 s to keep the pending-approval widget and avatar fresh.

### 6.8 Portfolio upload screening
Every upload runs automatically, in order:
1. EXIF read (a missing EXIF is only a note).
2. File stored.
3. **Local ONNX model (Detectra v3):** image resized to 384×384 (area-average), ImageNet-normalised, NCHW float32 → one logit → `p = sigmoid(logit)`. Verdict "ai" if `p ≥ 0.5`. **If `p ≥ reject-threshold` (default 0.50) the photo is `FLAGGED` with a plain-language `rejectionReason` shown to the photographer** ("…% confident this image was AI-generated…"). If the model is unavailable, the photo waits as `PENDING` for manual review — an unscreened upload is never auto-published.
4. **Duplicate check** against `VERIFIED` photos only: exact SHA-256 first, then perceptual hash (DCT-based, 32×32 luminance, 63 bits, median threshold) with Hamming distance ≤ 6 counting as a copy. The message says whether it was the photographer's own photo or someone else's.
5. Otherwise `VERIFIED` and live.
- Rejected photos stay visible in the admin panel; an admin can approve, reject, or run a paid deep check (AI-or-Not) at any time, including on already-published photos. The threshold choice is documented in `application.yml` (measured trade-off: 0.50 wrongly rejects ≈7% of genuine photos, 0.90 about 2.5% — the project owner chose 0.50 so AI images are actually caught, since rejections are visible to the photographer and reversible by an admin).

### 6.9 Blue badge
Optional paid verification badge (default 500 BDT, admin-set). Purchase via the payment pattern; grants top placement in search and a badge icon. Revoking does not refund. Admin analytics count all badge revenue including revoked ones.

### 6.10 Accessories Marketplace
- ~65 seeded products across 8 categories, prices in BDT (integers), sample photos bundled with credits; a few products fall back to line illustrations (`ProductArt`) when no free photo exists.
- Search (debounced), category chips with counts, sort (featured, price asc/desc, name, newest), infinite scroll (12 per page), filters kept in the URL.
- **Cart** stores only `[{productId, quantity}]` in `localStorage['shuttershot_cart']`, synced across tabs; names, prices and stock are always refetched, so a stale cart can never charge an old price. Max 20 per item.
- **Checkout** (guest allowed): name, phone (Bangladeshi, normalised to `0XXXXXXXXXX`), email, district, address, note; payment `CASH_ON_DELIVERY` or `SSLCOMMERZ`. District/address required only if any item is physical; email required for digital items.
- **Server recomputes everything:** prices from the DB, merged duplicate lines, delivery fee (**0** if no physical items or subtotal ≥ 10,000; **80** inside Dhaka; **150** elsewhere). Stock is decremented with an atomic conditional `UPDATE … WHERE stock >= qty` (never read-then-write); failure rolls back the whole order.
- Admin order pipeline `PLACED → CONFIRMED → SHIPPED → DELIVERED`, `CANCELLED` at any non-final point (restocks). An order awaiting online payment can only be cancelled until it is paid.

### 6.11 Suggestions
Public page with three topics: photoshoot category overview (admin-editable cards linking to search), pose gallery (5 subsections, admin-uploaded images), and music suggestions grouped by language for weddings plus event background music. Music entries are YouTube links only (validated host list, 11-char id); nothing plays inside the site.

### 6.12 AI features (all optional)
- **Full-page chat (`/chat`, Groq):** guests are stateless (the client resends history); signed-in users get stored conversations with a sidebar and delete. Reasoning model — `max_tokens` must stay large because reasoning tokens count against it; only `content` is ever read, never `reasoning`. The context lists at most ~40 photographers to stay under free-tier token limits.
- **Voice (`/speak`):** browser `MediaRecorder` → server transcription (Groq Whisper; the upload's filename extension matters because Whisper picks its decoder from it) → chat → browser `speechSynthesis` reads the reply.
- **Floating widgets** on every page (Gemini chat; browser speech recognition voice agent), opened by window events `open-chat-widget` / `open-voice-agent`.
- Without keys each feature just says it isn't configured; nothing else breaks.

### 6.13 Account deletion requests
Customers and photographers can request deletion of their own account (optional reason), see the pending/declined state, cancel, or resubmit. Staff accounts cannot. Admins/moderators review a queue ("Delete Account Request" in the sidebar) and approve or decline (with a note). Approval reuses the existing cascade delete: for a photographer it removes reviews, bookings, availability, badge, portfolio (including files on disk), packages and profile; bookings made as a customer are anonymised, not deleted; the email stays free to re-register.

### 6.14 Admin panel (sidebar)
General (any admin/moderator): Overview (stat tiles + charts + top earners), Review Moderation, Photo Moderation (tabs All/Verified/Flagged/Needs review, AI deep check), Manage pose, Manage Music, Photoshoot category manage, Manage Products, Manage Orders, User Management (verify; delete; ban = delete + block email), Delete Account Request.
Main-admin only (hidden from everyone else and enforced in the backend): Manage Admin, Manage Moderator, Photographers Profile History, Users Profile History (list/remove one/remove all; each removal re-asks for the admin's own password), Blue Badge Management, Booking Money.

### 6.15 Other
Light/dark theme (see §8.5), FAQ page, footer with support contact line. (An "Entertainment" page of mini-games was added in an early commit and is still mentioned in the README, but no such page exists in the current frontend — don't treat it as part of the product.)

---

## 7. REST API reference

Base `/api`. Unauthenticated → 401, wrong role → 403.

**Public:** `POST /auth/{register,register-customer,login,forgot-password,verify-reset-otp,reset-password}` · `POST /otp/{send,verify}` · `POST /chatbot/ask` · `POST /ai-chat/{messages,transcribe}` · `GET /photographers`, `/photographers/search`, `/photographers/{id}`, `/{id}/portfolio|packages|availability?from&to` · `GET /reviews?photographerId=` · `GET /poses`, `/music`, `/photoshoot-categories` · `GET /products`, `/products/categories`, `/products/by-ids?ids=`, `/products/photo-credits`, `/orders/delivery-rules` · `POST /orders` · `POST /bookings`, `/bookings/{id}/{verification-method,confirm-otp,deposit/initiate}`, `/bookings/verify-qr/{token}`, `GET /bookings/{id}` · `POST /reviews` · payment callbacks `POST /bookings/payment/{success,fail,cancel}`, `/shop/payment/{success,fail,cancel}`, `/blue-badge/payment/{success,fail,cancel}`.

**Authenticated (any role):** `GET|PUT /account/me`, `POST /account/me/photo` · `GET|POST|DELETE /account/deletion-request` · `GET /bookings/mine` · `GET /ai-chat/conversations`, `/conversations/{id}/messages`, `DELETE /ai-chat/conversations/{id}`.

**Photographer:** `GET /photographers/me`, `PUT /photographers/{id}`, `POST /photographers/me/photo` · packages CRUD `/packages` · `/portfolio` (GET, POST multipart, PUT caption, DELETE) · `PUT /availability/{date}` · `GET /bookings?photographerId=`, `PUT /bookings/{id}/status`, `GET /bookings/{id}/qr-code` · `GET /reviews/mine`, `PUT /reviews/{id}/reply` · `GET /blue-badge/status`, `POST /blue-badge/purchase`.

**Admin/moderator (`/admin/**`):** reviews (`GET`, `PUT /{id}/remove`) · photos (`GET ?status=`, `PUT /{id}/approve|reject`, `POST /{id}/ai-check`) · users (`GET ?role=`, `PUT /{id}/verify|ban`, `DELETE /{id}`) · `deletion-requests` (`GET`, `PUT /{id}/approve|reject`) · `overview` · `orders` (`GET`, `PUT /{id}/status`) · `music`, `photoshoot-categories`, `poses`, `products` CRUD (+ product image upload/remove).

**Main admin only (inside the service):** `/admin/staff` (GET/POST/DELETE) · `/admin/photographer-history` and `/admin/user-history` (GET; DELETE one or all with `{password}` body) · `/admin/blue-badge/settings` (GET/PUT price) and `/admin/blue-badge/holders` (+ revoke) · `/admin/booking-money/settings` (GET/PUT `{percent}`).

**Security matcher order (first match wins — keep this order):** permitAll auth/otp/uploads/chatbot → permit guest AI chat POSTs → authenticated rest of ai-chat → permitAll blue-badge payment → **authenticated `GET /photographers/me` before** permitAll `GET /photographers/**` → permitAll content/shop GETs and `POST /orders` → shop payment callbacks → permitAll booking POSTs → permitAll booking payment callbacks → **authenticated `GET /bookings/mine` before** permitAll `GET /bookings/*` → **`/admin/staff**` ADMIN-only before `/admin/**` ADMIN-or-MODERATOR → everything else authenticated.

---

## 8. Frontend specification

### 8.1 Routes
Public: `/` Home, `/search`, `/photographers/:id`, `/book/:photographerId`, `/verify-booking/:token`, `/suggestions`, `/chat`, `/speak`, `/faq`, `/marketplace`, `/marketplace/checkout`, `/login`, `/register`, `/register/user`, `/admin/login`, `/forgot-password`, `*` NotFound.
Photographer (`ProtectedRoute requiredRole="PHOTOGRAPHER"`, inside `DashboardLayout`): `/dashboard` and `/dashboard/{portfolio,calendar,packages,bookings,reviews,profile,verified-badge,delete-account}`.
Customer: `/account` (`CUSTOMER`). Staff (`['ADMIN','MODERATOR']`, inside `AdminLayout`): `/admin` (redirects to `/admin/overview`) and `/admin/{overview,reviews,photos,poses,music,categories,products,orders,users,deletion-requests,manage-admins,manage-moderators,photographer-history,user-history,blue-badge,booking-money}`.
`ProtectedRoute`: not logged in → `/admin/login` if staff roles are allowed, else `/login`; wrong role → the user's own area. Logout has no explicit navigate — clearing auth makes `ProtectedRoute` redirect.

### 8.2 Pages (behaviour worth reproducing)
- **Home:** hero with search, 3D camera card, page-flip photo showcases mixing bundled category photos with real approved portfolio photos (fetched a few photographers at a time, stopping once enough), 3-step "how it works", featured photographers, CTA.
- **BookingFlow:** four clickable step tabs; QR step polls; reads `?package=` and `?payment=`; handles loading / not-found / no-packages states.
- **Checkout:** stashes the order in `sessionStorage` before the gateway redirect and restores it on return; resets busy state on browser Back (`pageshow`); keeps the cart on failure/cancel, clears it on success.
- **VoiceChat:** status machine idle → recording → transcribing → thinking; releases the microphone after recording; cancels speech on unmount.
- **Admin Overview:** hand-built SVG charts (bar and donut) with nice-number axes, direct labels, hover tooltips and an accessible hidden data table.
- **Photo Moderation:** shows at most 24 at a time with "Show more (n left)".
- **Booking Money / Blue Badge:** form + live example; main-admin gate with a polite restricted view for others.

### 8.3 Components (selection)
`Navbar` (see 8.6), `Footer`, `DashboardLayout`/`AdminLayout` (+ `sidebarLinkStyles`), `ProtectedRoute`, `PillSelect` (custom `<select>` replacement with full keyboard support: arrows, Home/End, Enter/Space, Escape, type-ahead, outside-click close), `AvailabilityCalendar` (month navigation, interactive statuses configurable per use, local-date strings — never `toISOString`), `OtpInput` (6 boxes, auto-advance, paste), `PasswordField`, `QuantityStepper`, `RatingForm`, `StarRating` (fractional), `PortfolioGrid` (borderless masonry), `PhotoFlipBook` (stacked ping-pong layers, several transition styles), `Camera3D`, `ClickBurstLayer` (global click animation via a `click-burst` window event), `CartDrawer`, `ChatWidget`, `VoiceAgent`, `ChatMarkdown` (renders markdown as React elements; **never** `innerHTML`), `ConfirmPasswordModal`, `DeleteAccountSection` (shared by customers and photographers), `ImageUploader`, `ProfilePhotoUploader`, `ProductArt`, `BarChart`, `PieChart`, `BlueCheckIcon`, `AiSparkIcon`, `ThemeToggle`, `Logo`, `LogoutButton`.

### 8.4 State
- **AuthContext:** `user` from `localStorage['shuttershot_auth']`; `login()`, `logout()`. No refresh, no 401 interceptor (an expired token just fails until the user logs out — see §12).
- **CartContext:** see 6.10; also fetches delivery rules once; storage writes are try/catch-guarded for private mode.
- **ThemeContext:** key `shuttershot-theme` (`light`/`dark`), default light. An inline script in `index.html` sets `data-theme` on `<html>` **before first paint** to avoid a flash.
- Storage keys to know: `shuttershot_auth`, `shuttershot_cart`, `shuttershot-theme` (hyphen), `sessionStorage['shuttershot_last_order']`.

### 8.5 Design system
Warm terracotta brand, serif display type, rounded "capsule" controls, a genuinely designed dark theme (not an inverted filter). Defined as CSS custom properties in Tailwind v4's `@theme` block; the dark theme redefines the same variables under `:root[data-theme="dark"]`, so components use tokens only and need no dark-specific code.

| Token | Light | Dark |
|---|---|---|
| `--color-accent` | `#c15a3a` | `#e2865c` |
| `--color-accent-light` | `#d9825e` | `#eda37e` |
| `--color-ink` / `--color-ink-muted` | `#1e1b16` / `#7a7267` | `#f3efe8` / `#a89f92` |
| `--color-surface` / `--color-surface-raised` | `#ffffff` / `#fcfaf7` | `#221e19` / `#2b2620` |
| `--color-border` | `#e8e2d9` | `#3a3229` |
| `--color-free` (available) | `#8db5a0` | `#8fc0a9` |
| `--color-booked` | `#c47a6e` | `#e08d7d` |
| `--canvas-from` → `--canvas-to` | `#fbf7f0` → `#f5eee6` | `#15120e` → `#1b1713` |
| `--shadow-card` / `--shadow-hover` | `0 4px 20px rgba(0,0,0,.04)` / `0 8px 30px rgba(0,0,0,.08)` | `…,.45` / `…,.6` |
| `--radius-card` | `0.5rem` | same |

- **Fonts:** Fraunces (display, 300–900, optical size) for headings; Work Sans (400/500/600) for body — loaded from Google Fonts.
- **Conventions:** every control is a capsule (`rounded-full`, hairline border, `bg-surface`; active = accent gradient + white text; hover = accent border/text). Data cards `rounded-card bg-surface shadow-card`, hover lifts. Photos are borderless. Status chips are tinted pills. Page shell `flex min-h-screen flex-col bg-canvas`, content `max-w-6xl px-6 sm:px-12`. Calm, warm copy; empty states are friendly; errors never blame the user.
- **Motion:** per-link click animations (flicker, wiggle, bounce, flash, pulse, glow), ambient floating blobs, gradient flow, a click-burst effect, camera drift. A `prefers-reduced-motion` rule disables them.
- **Global CSS rules:** visible `:focus-visible` rings (a `.pill-focus` ring follows rounded corners; `.bare-input` for inputs inside a capsule whose container shows the ring); `body` uses `overflow-x: clip`, **not** `hidden` (hidden breaks `position: sticky`).
- Some colours deliberately don't flip in dark mode (accent gradient, shipped-status tint, shop category tints).

### 8.6 Header / navigation behaviour (hard-won)
A single slim capsule contains: AI icon, Speak with AI, Chat with AI, Suggestions, Find a photographer, FAQs, then (logged out) Admin portal, Join as photographer, **cart icon**, Log in — or (logged in) cart and the user's own-area button. Cart icon sits between "Join as photographer" and "Log in". Text is `text-sm`; capsule padding generous enough to look like a capsule, not a flat bar. At **≥900px** the capsule shows inline (it stays on one line down to roughly 1280px and wraps tidily to 2–3 lines below that); **below 900px** it collapses to logo + cart + menu button + theme toggle with a dropdown panel. Do not switch to the mobile menu earlier than that — it was tried at 1024px and rejected as too aggressive. Delegated header-level click handling triggers the burst animation for every link automatically.

---

## 9. Configuration (environment variables, all optional)

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `jdbc:postgresql://localhost:5433/shuttershot`, `postgres`, `postgres` | Database |
| `JWT_SECRET`, `JWT_EXPIRATION_MS` | dev placeholder, 86400000 | Token signing (override beyond local dev) |
| `UPLOAD_DIR` | `uploads/portfolio` | Where uploads are stored |
| `APP_BASE_URL`, `APP_FRONTEND_URL` | `http://localhost:8080`, `http://localhost:5173` | URLs baked into QR codes, uploaded-file URLs, emails and gateway redirects |
| `MAIL_HOST/PORT/USERNAME/PASSWORD/FROM` | gmail SMTP, empty creds | Password-reset and staff-credential emails; unset → sending fails quietly and the reset link is logged |
| `GEMINI_API_KEY`, `GEMINI_MODEL` | empty | Floating chat widget |
| `GROQ_API_KEY`, `GROQ_MODEL`, `GROQ_TRANSCRIPTION_MODEL` | empty, `openai/gpt-oss-120b`, `whisper-large-v3-turbo` | Full-page chat and voice |
| `AIORNOT_API_KEY` | empty | Admin "deep check" on a photo |
| `DETECTRA_MODEL_PATH`, `DETECTRA_REJECT_THRESHOLD` | `models/detectra-v3/model.onnx`, `0.50` | Local AI-image screening |
| `DUPLICATE_MAX_DISTANCE` | `6` | Perceptual-hash distance counted as the same photo |
| `SSLCOMMERZ_STORE_ID`, `SSLCOMMERZ_STORE_PASSWORD`, `SSLCOMMERZ_SANDBOX` | empty, empty, `true` | Payments |
| `TEXTBELT_API_KEY` | `textbelt` (free tier, 1 SMS/day) | SMS OTP |
| Frontend: `VITE_API_BASE_URL` | `http://localhost:8080/api` | API base |

Every optional integration reports itself as unavailable rather than breaking the rest of the site.

---

## 10. Testing and verification approach

**Automated (58 backend tests, JUnit 5 + Mockito):** order placement and totals, delivery fees, phone normalisation, stock rollback, the full online-payment state machine (paid, failed, cancelled, late payment, refund-flag, expiry), photoshoot categories, YouTube link parsing, product seeding invariants (every item sellable, unique names, sample images exist), the booking-money service (default rate, reading never writes, only main admin can change), and a Spring context smoke test. **Not** covered by automated tests: the booking lifecycle, OTP/TOTP, SSLCommerz client, upload screening, fingerprints, reviews, search, security rules, AI chat.

**Manual / scripted verification that was part of the process (reproduce this habit):**
- Run the real stack (Postgres, backend, frontend) and drive it with real requests: register throwaway accounts, exercise the endpoint, assert status codes and bodies, **then delete every test row**.
- Test access control explicitly: no token, wrong role, non-main admin, main admin.
- Test validation boundaries (0, 0.99, 1, 100, 100.01, too many decimals, negative, missing, wrong type).
- Test the invariant that matters, not just the happy path (e.g. after changing the deposit rate, an *existing* booking still shows its original percent and amount).
- Drive the UI in a real browser (Playwright with Edge/Chromium): log in by injecting a session, click through, screenshot, and look at the screenshots.
- Decode real artefacts rather than reading code: the QR images were decoded with a QR reader to see what they actually contained — this found two bugs (§11).
- Compare against the closest existing endpoint when a test fails, to tell "my bug" from "pre-existing behaviour".
- Always finish with a production build of the frontend and the full test suite.

---

## 11. Lessons learned (bugs found, and why)

1. **Category search always returned zero.** The filter matched a "specialties" field that nothing in the app ever set (no UI wrote it). Fix: match against verified portfolio photos — real data that already existed. *Lesson: a filter is only as good as the data it reads; check the data exists and has a writer.*
2. **TOTP QR was unusable.** The authenticator library has two similarly named methods; `getOtpAuthURL` returns a link to a third-party image generator, `getOtpAuthTotpURL` returns the raw `otpauth://totp/…` secret. The QR must encode the raw URI. *Lesson: decode generated QR codes and look at the payload.*
3. **QR codes pointed at `localhost`.** A phone cannot reach the dev machine's `localhost`. Fix: LAN IP in `APP_FRONTEND_URL` / `APP_BASE_URL` / `VITE_API_BASE_URL`, Vite bound to all interfaces, CORS patterns for private LAN ranges.
4. **Detectra detected AI but didn't reject.** The reject threshold was 0.90 while the verdict threshold is 0.50, so 50–89% "AI" photos were published. Threshold lowered to 0.50 by the owner's decision after measuring the false-reject trade-off.
5. **Payment callbacks blocked by CORS.** The gateway posts cross-origin from its own domain; each callback route needs its own permissive CORS entry registered before the general rule.
6. **Hibernate `ddl-auto: update` never edits existing CHECK constraints.** Adding an enum value (`SSLCOMMERZ`) to a column created earlier fails at insert time. A startup runner drops the stale constraint. *Lesson: when you add an enum value to an existing table, check for a check constraint.*
7. **`overflow-x: hidden` on `body` breaks `position: sticky`.** Use `overflow-x: clip`.
8. **Responsive nav that "wraps" is not a mobile design.** Many inline links wrapped into a ragged multi-line blob. Measure the real breakpoints with screenshots at many widths (it stays tidy to ~900px, fits one line to ~1280px) instead of guessing, and keep the compact menu for genuinely narrow widths only.
9. **A flat capsule looks like a stick.** When shrinking text, keep vertical padding generous so the rounded ends still read as a capsule.
10. **A browser cannot tell a narrow desktop window from a phone.** "Never reflow" would also freeze the layout on real phones — a product trade-off to put to the owner explicitly.
11. **Reasoning models need a big `max_tokens`** or they return empty content; never expose their reasoning field.
12. **dHash collided on low-detail images**; DCT-based perceptual hash was used instead.
13. **Whisper picks its decoder from the uploaded filename extension.**
14. **Approving account deletion must remove the request row first, in the same transaction as the cascade delete**, or the foreign key blocks it; the transaction rolls both back if anything fails.
15. **Don't create-on-read singleton settings rows** (race on first access, writes inside read-only transactions). Read with a default; only the admin update upserts.
16. **PowerShell/Windows tooling quirks** that cost time: quoted multi-line commit messages break argument parsing (use `git commit -F file`); line-ending (LF/CRLF) noise makes unchanged files show as modified (compare bytes before committing); killing processes from another session may be denied; background tasks have time caps.

---

## 12. Known weaknesses — do NOT replicate these in a new project

- **Plaintext password copy.** `users.stored_password` keeps a plaintext copy of every password so the main admin can look it up ("history" pages). This is a serious security flaw. Use hashing only and a reset flow.
- **`GET /api/bookings/{id}` is public** and returns client PII; **`POST /api/reviews` has no ownership check** (anyone who knows a completed booking id can review it); the OTP code is echoed in the response (a workaround for an SMS provider limit). Add authorisation or unguessable booking tokens.
- Upload validation trusts the content-type header only (no magic-byte check); all uploads share one directory; file URLs are stored absolute, so changing the base URL breaks old files.
- `timeSlot` is free text and only the **date** is locked, so a photographer can be booked once per day.
- The gateway validation checks the amount but not that the validation record belongs to the transaction.
- No frontend 401 handling, no token refresh, no cross-tab auth sync; no frontend tests; no TypeScript or linting; footer social links are placeholders.
- Test coverage gaps listed in §10 (booking lifecycle, security rules, screening).
- The home page "4.9 average rating" is a fixed placeholder, not a live figure.
- The `specialties` field on photographers still exists (unused) and is displayed on cards if ever set.
- Photo moderation lists load in portions client-side rather than paging on the server.
- SSLCommerz runs against its sandbox by default; live payments need real credentials and `SSLCOMMERZ_SANDBOX=false`.

---

## 13. Recommended build order for a similar project

1. **Scaffold both apps** as independent folders in one repo: Spring Boot 3 + Java 17 + PostgreSQL, and React + Vite + Tailwind; REST between them; Docker Compose for the database; a `SETUP.md` that works on a fresh clone with zero configuration (sensible defaults for every env var).
2. **Auth and roles first:** users, JWT, a customer-like role, a provider-like role, admin/moderator, password reset. Decide the sensitive "main admin" concept now.
3. **The core listing and profile:** the provider's profile, search/filter (data-driven, tested against real data), public detail page.
4. **The core transaction:** the full booking lifecycle with an explicit status machine, one verification method, availability locking — end to end, before adding a second method.
5. **The payment pattern (§6.4) built once** as a reusable service, then reused for every place money is taken.
6. **Provider dashboard**, then **admin panel**, then secondary features (shop, AI, content/suggestions, extra verification methods).
7. **Design tokens and UI conventions (§8.5) from the first screen**, light and dark together; don't retrofit dark mode.
8. **Admin-configurable values** (prices, rates) as singleton settings rows with safe defaults, applied at creation time and snapshotted on the record they affect.
9. **Working-style conventions (§14) from the first commit.**

---

## 14. Working style and quality bar

These are standing preferences of the project owner, learned over the whole build. Apply them unless told otherwise.

- **Do exactly what was asked — nothing else.** "Add this feature only. Do not touch or ruin anything else." Keep changes surgical; match existing patterns instead of inventing new ones. If something outside the request looks wrong, mention it; don't fix it unasked.
- **Look before building.** Find the closest existing pattern (e.g. the blue-badge price setting before building the booking-money setting) and follow it: same layering, naming, security approach, UI structure.
- **Verify for real, not by assertion.** Compile, run the tests, run the app, exercise the feature with real requests, drive the UI in a browser, and look at screenshots. Say plainly what was tested and what could not be.
- **Test the dangerous cases:** access control, validation boundaries, the invariant the feature must preserve, and cleanup of every row you created.
- **Never trust the client with money or stock.** Recalculate prices, totals and stock on the server on every request that touches money; use atomic conditional updates; confirm payments server-to-server.
- **Reversible beats clever.** Prefer approaches that are easy to walk back; keep defaults identical to the old behaviour until a person changes them.
- **"Do it better" is a real instruction.** With high-level direction, use good judgement on layout, copy and polish — but flag anything that goes beyond what was asked rather than slipping it in.
- **Iterative visual feedback.** The owner reviews screenshots and gives specific corrections ("keep the old version", "centre this", "too slim", "it should look like a capsule"). Treat each correction as a durable preference for the rest of the project, not a one-off. When a correction is ambiguous, measure and show (screenshots at several sizes) rather than guess a third time.
- **Ask when a decision is genuinely theirs** (e.g. who may change a money setting; whether to freeze the layout or keep it responsive), with the trade-off stated plainly and a recommended option first. Otherwise decide, proceed, and say what you decided.
- **Report outcomes plainly:** what changed, how it was verified, what passed, honest limitations, and anything pre-existing you noticed but left alone.
- **Don't commit or push unless explicitly asked,** and then only the intended files (exclude build artefacts and local-only files); verify the result on the remote afterwards.
- **Keep secrets out:** secret values are checked for presence, never printed; `.env` files are git-ignored.
- **Security posture:** passwords hashed only, role checks per endpoint plus service-level guards for the most sensitive actions, destructive actions confirmed, nothing from the browser trusted for authorisation or money.
