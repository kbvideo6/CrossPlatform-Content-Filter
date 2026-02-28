package com.ultraguard.dns

/**
 * ═══════════════════════════════════════════════════════════════
 *  DOMAIN BLOCK LIST
 * ═══════════════════════════════════════════════════════════════
 *
 *  Central reference list of blocked domains. Cloudflare Family
 *  DNS handles the actual blocking at the resolver level.
 *  This list is used for display/analytics purposes.
 *
 *  Matching is done on the queried domain and all parent domains,
 *  so blocking "example.com" will also block "www.example.com",
 *  "m.example.com", "api.example.com", etc.
 */
object DomainBlockList {

    private val blockedDomains: Set<String> = setOf(
        // ── Tube / streaming sites ──
        "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "spankbang.com",
        "chaturbate.com", "erome.com", "redtube.com", "youporn.com", "tube8.com",
        "eporner.com", "txxx.com", "onlyfans.com", "cam4.com",
        "stripchat.com", "hqporner.com", "daftsex.com", "bravotube.net", "drtuber.com",
        "fapster.xxx", "javhd.com", "nuvid.com", "pornhd.com", "porntrex.com",
        "sunporno.com", "tnaflix.com", "upornia.com", "vporn.com",
        "vjav.com", "yourporn.sexy", "zizt.com", "alohatube.com", "cumlouder.com",
        "empflix.com", "fuckhub.tv", "gotuby.com", "hdpornbox.com", "javfinder.com",
        "letmejerk.com", "mrskin.com", "perfectgirls.net", "porndoe.com",
        "pornhexxx.com", "slutload.com", "subsmovies.tv", "swinger.com", "xhamsterlive.com",
        "xxxmvi.com", "beeg.com", "cliphunter.com", "efukt.com", "extremetube.com",
        "freeones.com", "hardsextube.com", "heavenpass.com", "imagefap.com", "jizzbunker.com",
        "keezmovies.com", "laidhub.com", "madthumbs.com", "mofosex.com", "moviesand.com",
        "myporner.com", "noodlemagazine.com", "onejav.com", "pichunter.com", "pornhdfree.com",
        "porno.com", "pornve.com", "redgifs.com", "shufflesex.com",
        "sxyprn.com", "thumbzilla.com", "videoz.com",

        // ── Premium / studio sites ──
        "anyporn.com", "bangbros.com", "brazzers.com", "digitalplayground.com", "evilangel.com",
        "hustler.com", "julesjordan.com", "kink.com", "men.com",
        "mofos.com", "naughtyamerica.com", "netgirls.com", "penthouse.com", "playboy.com",
        "pornstarspa.com", "private.com", "realitykings.com", "sexyhub.com", "teamskeet.com",
        "titansmen.com", "twistys.com", "x-art.com", "zerotolerance.com",

        // ── Live cam sites ──
        "amateur.tv", "cams.com", "camsoda.com", "flirt4free.com", "imlive.com", "jasmin.com",
        "livejasmin.com", "myfreecams.com", "xmodels.com", "fansly.com", "manyvids.com",
        "justforfans.com", "loyalfans.com", "adultwork.com", "bonga.com",
        "camster.com", "chaturbate.tv", "dirtyroulette.com", "fancentro.com", "modelhub.com",
        "naked.com", "streammate.com", "voyeurweb.com", "webcams.com", "xlovers.com",

        // ── Hentai / drawn ──
        "hentai-foundry.com", "hentaihaven.com", "nhentai.net", "rule34.pro", "sankakucomplex.com",
        "fakku.net", "gelbooru.com", "rule34.xxx", "xbooru.com", "danbooru.donmai.us",
        "yande.re", "paheal.net", "3dhentai.com", "adult-fanfiction.org", "bearchive.com",
        "chan.sankakucomplex.com", "doujinshi.org", "e-hentai.org", "exhentai.org", "furaffinity.net",
        "hentai2read.com", "hentaigazette.com", "hentaihere.com", "luscious.net", "multporn.net",
        "nhentai.com", "pururin.io", "simply-hentai.com", "tbib.org", "tsumino.com",
        "vrsmash.com", "wnacg.org", "yaoihavenreborn.com", "8muses.com", "9hentai.to",
        "animeidhentai.com", "asmhentai.com", "doujins.com", "fakku.com", "g.e-hentai.org"
    )

    /**
     * Check if a domain (or any of its parent domains) is in the block list.
     *
     * Example: "www.pornhub.com" → checks "www.pornhub.com", "pornhub.com", "com"
     */
    fun isBlocked(domain: String): Boolean {
        val normalized = domain.lowercase().trimEnd('.')

        // Check exact match first
        if (normalized in blockedDomains) return true

        // Check parent domains (e.g., subdomain.blocked.com → blocked.com)
        val parts = normalized.split(".")
        for (i in 1 until parts.size - 1) {
            val parent = parts.subList(i, parts.size).joinToString(".")
            if (parent in blockedDomains) return true
        }

        return false
    }

    /**
     * Get the total number of blocked domains.
     */
    fun count(): Int = blockedDomains.size
}
