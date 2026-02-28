import os

HOSTS = r"C:\Windows\System32\drivers\etc\hosts"

BLOCK_LIST = [
    "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "spankbang.com",
    "chaturbate.com", "erome.com", "redtube.com", "youporn.com", "tube8.com",
    "eporner.com", "txxx.com", "bitchute.com", "onlyfans.com", "cam4.com",
    "stripchat.com", "hqporner.com", "daftsex.com", "bravotube.net", "drtuber.com",
    "fapster.xxx", "javhd.com", "nuvid.com", "pornhd.com", "porntrex.com",
    "spiegel.com", "sunporno.com", "tnaflix.com", "upornia.com", "vporn.com",
    "vjav.com", "yourporn.sexy", "zizt.com", "alohatube.com", "cumlouder.com",
    "empflix.com", "fuckhub.tv", "gotuby.com", "hdpornbox.com", "javfinder.com",
    "letmejerk.com", "metacafe.com", "mrskin.com", "perfectgirls.net", "porndoe.com",
    "pornhexxx.com", "slutload.com", "subsmovies.tv", "swinger.com", "xhamsterlive.com",
    "xxxmvi.com", "beeg.com", "cliphunter.com", "efukt.com", "extremetube.com",
    "freeones.com", "hardsextube.com", "heavenpass.com", "imagefap.com", "jizzbunker.com",
    "keezmovies.com", "laidhub.com", "madthumbs.com", "mofosex.com", "moviesand.com",
    "myporner.com", "noodlemagazine.com", "onejav.com", "pichunter.com", "pornhdfree.com",
    "porno.com", "pornve.com", "redgifs.com", "shufflesex.com", "smooch.com",
    "sorgilla.com", "sxyprn.com", "thumbzilla.com", "tubemate.com", "videoz.com",
    "anyporn.com", "bangbros.com", "brazzers.com", "digitalplayground.com", "evilangel.com",
    "hustler.com", "julesjordan.com", "kink.com", "men.com", "milerotica.com",
    "mofos.com", "naughtyamerica.com", "netgirls.com", "penthouse.com", "playboy.com",
    "pornstarspa.com", "private.com", "realitykings.com", "sexyhub.com", "teamskeet.com",
    "titansmen.com", "twistys.com", "x-art.com", "zerotolerance.com", "amateur.tv",
    "cams.com", "camsoda.com", "flirt4free.com", "imlive.com", "jasmin.com",
    "livejasmin.com", "myfreecams.com", "xmodels.com", "fansly.com", "manyvids.com",
    "patreon.com", "justforfans.com", "loyalfans.com", "adultwork.com", "bonga.com",
    "camster.com", "chaturbate.tv", "dirtyroulette.com", "fancentro.com", "modelhub.com",
    "naked.com", "streammate.com", "voyeurweb.com", "webcams.com", "xlovers.com",
    "hentai-foundry.com", "hentaihaven.com", "nhentai.net", "rule34.pro", "sankakucomplex.com",
    "fakku.net", "gelbooru.com", "rule34.xxx", "xbooru.com", "danbooru.donmai.us",
    "yande.re", "paheal.net", "3dhentai.com", "adult-fanfiction.org", "bearchive.com",
    "chan.sankakucomplex.com", "doujinshi.org", "e-hentai.org", "exhentai.org", "furaffinity.net",
    "hentai2read.com", "hentaigazette.com", "hentaihere.com", "luscious.net", "multporn.net",
    "nhentai.com", "pururin.io", "simply-hentai.com", "tbib.org", "tsumino.com",
    "vrsmash.com", "wnacg.org", "yaoihavenreborn.com", "8muses.com", "9hentai.to",
    "animeidhentai.com", "asmhentai.com", "doujins.com", "fakku.com", "g.e-hentai.org"
]

def enforce_hosts():
    print("Enforcing DNS-level blocking via hosts file...")
    try:
        with open(HOSTS, "r+") as f:
            content = f.read()
            for site in BLOCK_LIST:
                if site not in content:
                    f.write(f"\n127.0.0.1 {site}")
                    f.write(f"\n127.0.0.1 www.{site}")
        print("Hosts file updated successfully.")
    except PermissionError:
        print("Permission Denied: Run as Administrator to modify hosts file.")
    except Exception as e:
        print(f"Failed to update hosts file: {e}")

if __name__ == "__main__":
    enforce_hosts()