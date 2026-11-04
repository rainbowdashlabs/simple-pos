# Simple POS

A mainly educational project to learn vuejs build with vite. Additionally use of tailwind for styling.

### Configuration

The application reads `application.yaml` from its working directory (`/app/application.yaml` in the Docker image)
in addition to the bundled defaults. Besides the datasource, one setting is required:

| Property (environment variable)                       | Default            | Meaning                                                                                                   |
|-------------------------------------------------------|--------------------|-----------------------------------------------------------------------------------------------------------|
| `security.jwt.secret-key` (`SECURITY_JWT_SECRET_KEY`) | none, required     | Signs all login tokens. Use a random value of at least 32 bytes, e.g. `openssl rand -hex 32`. Keep it secret. |
| `security.jwt.expiration-time` (`SECURITY_JWT_EXPIRATION_TIME`) | `3600000` (1 h)    | Lifetime of a login token in milliseconds.                                                                |
| `security.jwt.refresh-expiration-time` (`SECURITY_JWT_REFRESH_EXPIRATION_TIME`) | `2592000000` (30 d) | Lifetime of a refresh token in milliseconds. Every refresh issues a new one, so a session stays alive as long as it is used within this time.                              |

Changing the secret key logs out every user. Without a key the application does not start. The `local` Spring
profile and the `dev` profile of `compose.yml` come with a key that is only meant for development.

The frontend is served by the application itself. Other origins may call the API only when listed in
`security.cors.allowed-origins` (comma separated); by default none may.

Logins are rate limited per client address. Behind a reverse proxy such as traefik the address is taken from
`X-Forwarded-For` (`server.forward-headers-strategy: native`), but only for connections from internal addresses
(`server.tomcat.remoteip.internal-proxies`: by default private, loopback, link-local, carrier-grade NAT and IPv6
unique-local ranges). Make sure only the proxy can reach the application, or set
`server.tomcat.remoteip.internal-proxies` to the proxy's address; otherwise anything on those networks can claim any
client address.

### Resources:
Vue.JS https://vuejs.org/guide/introduction.html
Vue i18n https://vue-i18n.intlify.dev/guide/essentials/started
Tailwind https://tailwindcss.com/docs/installation
Fontawesome Icon Search https://fontawesome.com/icons

https://dbdiagram.io/d/Simple-POS-669902888b4bb5230eb2e9b6
