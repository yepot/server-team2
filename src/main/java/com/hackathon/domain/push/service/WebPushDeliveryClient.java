package com.hackathon.domain.push.service;

import com.hackathon.domain.push.config.PushVapidProperties;
import com.hackathon.domain.push.entity.PushSubscription;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class WebPushDeliveryClient implements WebPushClient {

	private final PushVapidProperties pushVapidProperties;
	private volatile PushService pushService;

	@Override
	public WebPushSendResult send(PushSubscription pushSubscription, String payload) {
		try {
			Notification notification = new Notification(
					pushSubscription.getEndpoint(),
					pushSubscription.getP256dh(),
					pushSubscription.getAuth(),
					payload.getBytes(StandardCharsets.UTF_8),
					pushVapidProperties.ttlSeconds()
			);
			HttpResponse response = getPushService().send(notification, Encoding.AES128GCM);

			return new WebPushSendResult(
					response.getStatusLine().getStatusCode(),
					extractResponseBody(response)
			);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Web Push 발송 중 인터럽트가 발생했습니다.", exception);
		} catch (GeneralSecurityException | IOException | JoseException | ExecutionException exception) {
			throw new IllegalStateException("Web Push 발송에 실패했습니다.", exception);
		}
	}

	private PushService getPushService() {
		PushService current = pushService;
		if (current != null) {
			return current;
		}

		synchronized (this) {
			if (pushService == null) {
				pushService = createPushService();
			}
			return pushService;
		}
	}

	private PushService createPushService() {
		if (!pushVapidProperties.isConfigured()) {
			throw new IllegalStateException("Web Push 설정이 완료되지 않았습니다.");
		}

		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}

		try {
			return new PushService(
					pushVapidProperties.publicKey(),
					pushVapidProperties.privateKey(),
					pushVapidProperties.subject()
			);
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("Web Push 초기화에 실패했습니다.", exception);
		}
	}

	private String extractResponseBody(HttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			return null;
		}

		return EntityUtils.toString(entity, StandardCharsets.UTF_8);
	}
}
