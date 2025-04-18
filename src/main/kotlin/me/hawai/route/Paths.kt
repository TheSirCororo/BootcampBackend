@file:Suppress("UNUSED")

package ru.cororo.corasense.route

import io.ktor.resources.*

object Paths {
    @Resource("/clients")
    class Clients {
        @Resource("/{clientId}")
        class ById(val clientId: String, val parent: Clients = Clients())

        @Resource("/bulk")
        class Bulk(val parent: Clients = Clients())
    }

    @Resource("/advertisers")
    class Advertisers {
        @Resource("/bulk")
        class Bulk(val parent: Advertisers = Advertisers())

        @Resource("/{advertiserId}")
        class ById(val advertiserId: String, val parent: Advertisers = Advertisers()) {
            @Resource("/campaigns")
            class Campaigns(val advertiserId: String, val parent: ById = ById(advertiserId)) {
                @Resource("/{campaignId}")
                class CampaignById(
                    val advertiserId: String,
                    val campaignId: String,
                    val parent: Campaigns = Campaigns(advertiserId)
                )
            }
        }
    }

    @Resource("/ads")
    class Ads {
        @Resource("/{adId}/click")
        class Click(val adId: String, val parent: Ads = Ads())
    }

    @Resource("/stats")
    class Stats {
        @Resource("/campaigns/{campaignId}")
        class Campaigns(val campaignId: String, val parent: Stats = Stats()) {
            @Resource("/daily")
            class Daily(val campaignId: String, val parent: Campaigns = Campaigns(campaignId))
        }

        @Resource("/advertisers/{advertiserId}/campaigns")
        class Advertisers(val advertiserId: String, val parent: Stats = Stats()) {
            @Resource("/daily")
            class Daily(val advertiserId: String, val parent: Advertisers = Advertisers(advertiserId))
        }
    }

    @Resource("/images")
    class Images {
        @Resource("/{imageId}")
        class ById(val imageId: String, val parent: Images = Images())
    }

    @Resource("/llm")
    class Llm {
        @Resource("/campaign-text")
        class GenerateCampaignText(val parent: Llm = Llm())
    }

    @Resource("/blocked-words")
    class BlockedWords

    @Resource("/moderation")
    class Moderation {
        @Resource("/mode")
        class Mode(val parent: Moderation = Moderation())

        @Resource("/enable")
        class Enable(val parent: Moderation = Moderation())

        @Resource("/disable")
        class Disable(val parent: Moderation = Moderation())
    }

    @Resource("/ml-scores")
    class MLScores

    @Resource("/time/advance")
    class TimeAdvance

    @Resource("/ping")
    class Ping
}