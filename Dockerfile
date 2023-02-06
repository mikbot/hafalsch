FROM gradle:jdk19 as builder
WORKDIR /usr/app
COPY . .
RUN gradle --no-daemon bot:installBotArchive

FROM ibm-semeru-runtimes:open-18-jre-focal

WORKDIR /usr/app
COPY --from=builder /usr/app/bot/build/installBot .

LABEL org.opencontainers.image.source = "https://github.com/mikbot/hafalsch"

ENTRYPOINT ["/usr/app/bin/mikmusic"]
