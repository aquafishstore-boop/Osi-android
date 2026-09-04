# Build OSIRIS Android debug APK inside Docker (no host JDK/SDK required).
# Usage:
#   docker compose -f docker-compose.build.yml build
#   docker compose -f docker-compose.build.yml run --rm android-build
# Output: ./artifacts/app-debug.apk

FROM eclipse-temurin:17-jdk-jammy

ENV DEBIAN_FRONTEND=noninteractive \
    ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    GRADLE_VERSION=8.9 \
    PATH=/opt/gradle/gradle-8.9/bin:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:/opt/android-sdk/build-tools/35.0.0:$PATH

RUN apt-get update && apt-get install -y --no-install-recommends \
      curl unzip git ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Gradle (avoid broken Windows CRLF wrapper scripts)
RUN curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o /tmp/gradle.zip \
    && unzip -q /tmp/gradle.zip -d /opt/gradle \
    && rm /tmp/gradle.zip

# Android command-line tools
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o /tmp/cmdtools.zip \
    && unzip -q /tmp/cmdtools.zip -d ${ANDROID_HOME}/cmdline-tools \
    && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm /tmp/cmdtools.zip

RUN yes | sdkmanager --licenses >/dev/null \
    && sdkmanager \
      "platform-tools" \
      "platforms;android-35" \
      "build-tools;35.0.0"

WORKDIR /workspace
COPY . /workspace

RUN printf "sdk.dir=/opt/android-sdk\n" > local.properties

RUN gradle --no-daemon assembleDebug testDebugUnitTest \
    && mkdir -p /artifacts \
    && cp app/build/outputs/apk/debug/*.apk /artifacts/app-debug.apk \
    && (cp -r app/build/test-results /artifacts/test-results || true) \
    && ls -la /artifacts

CMD ["ls", "-la", "/artifacts"]
