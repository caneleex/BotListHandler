package dev.mlnr.blh.internal;

import dev.mlnr.blh.api.BotList;
import dev.mlnr.blh.api.BotListHandler;
import dev.mlnr.blh.internal.config.AutoPostingConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BLHImpl implements BotListHandler {
	private final Map<BotList, String> botLists;
	private final AutoPostingConfig autoPostingConfig;

	private static final Logger logger = LoggerFactory.getLogger(BLHImpl.class);
	private final OkHttpClient httpClient = new OkHttpClient();

	private long previousGuildCount = -1;

	public BLHImpl(Map<BotList, String> botListMap, AutoPostingConfig autoPostingConfig) {
		this.botLists = botListMap;
		this.autoPostingConfig = autoPostingConfig;

		if (isAutoPostingEnabled()) {
			ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

			JDA jda = autoPostingConfig.getJDA();
			long delay = autoPostingConfig.getDelay();
			long initialDelay = delay;
			if (jda.getStatus() == JDA.Status.INITIALIZED) // if jda has finished setting up cache for guilds, immediately post the guild count
				initialDelay = 0;
			scheduler.scheduleAtFixedRate(() -> updateStats(jda), initialDelay, delay, autoPostingConfig.getUnit());
		}
	}

	@Override
	public boolean isAutoPostingEnabled() {
		return autoPostingConfig.isAutoPostingEnabled();
	}

	@Override
	public void updateStats(JDA jda) {
		long serverCount = jda.getGuildCache().size();
		if (serverCount == previousGuildCount) {
			logger.info("No stats updating was necessary.");
			return;
		}
		previousGuildCount = serverCount;

		botLists.forEach((botList, token) -> {
			final String botListName = botList.name();

			DataObject payload = DataObject.empty().put(botList.getServersParam(), serverCount);

			String url = String.format(botList.getUrl(), jda.getSelfUser().getId());
			Request.Builder requestBuilder = new Request.Builder().url(url)
					.header("Authorization", token)
					.post(RequestBody.create(MediaType.parse("application/json"), payload.toString()));

			httpClient.newCall(requestBuilder.build()).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {
					logger.error("There was an error while updating stats for bot list {}", botListName, e);
				}

				@Override
				public void onResponse(Call call, Response response) {
					if (response.isSuccessful()) {
						logger.info("Successfully updated stats for bot list {}", botListName);
					}
					else {
						logger.error("Failed to update stats for bot list {} with code {}", botListName, response.code());
					}
				}
			});
		});
	}
}