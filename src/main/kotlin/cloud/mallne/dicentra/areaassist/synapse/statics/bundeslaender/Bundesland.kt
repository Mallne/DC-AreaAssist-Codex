package cloud.mallne.dicentra.areaassist.synapse.statics.bundeslaender

@Suppress("SpellCheckingInspection")
enum class Bundesland(
    val definition: BundeslandDefinition,
    val iso3166_2: String,
    val deBId: Int? = null
) {
    SCHLESWIG_HOLSTEIN(
        definition = SchleswigHolstein,
        iso3166_2 = "DE-SH",
        deBId = 1
    ),
    HAMBURG(
        definition = Hamburg,
        iso3166_2 = "DE-HH",
        deBId = 2
    ),
    NIEDERSACHSEN(
        definition = Niedersachsen,
        iso3166_2 = "DE-NI",
        deBId = 3
    ),
    BREMEN(
        definition = Bremen,
        iso3166_2 = "DE-HB",
        deBId = 4
    ),
    NORDRHEIN_WESTFALEN(
        definition = NordrheinWestfalen,
        iso3166_2 = "DE-NW",
        deBId = 5
    ),
    HESSEN(
        definition = Hessen,
        iso3166_2 = "DE-HE",
        deBId = 6
    ),
    RHEINLAND_PFALZ(
        definition = RheinlandPfalz,
        iso3166_2 = "DE-RP",
        deBId = 7
    ),
    BADEN_WUERTTEMBERG(
        definition = BadenWuerttemberg,
        iso3166_2 = "DE-BW",
        deBId = 8
    ),
    BAYERN(
        definition = Bayern,
        iso3166_2 = "DE-BY",
        deBId = 9
    ),
    SAARLAND(
        definition = Saarland,
        iso3166_2 = "DE-SL",
        deBId = 10
    ),
    BERLIN(
        definition = Berlin,
        iso3166_2 = "DE-BE",
        deBId = 11
    ),
    BRANDENBURG(
        definition = Brandenburg,
        iso3166_2 = "DE-BB",
        deBId = 12
    ),
    MECKLENBURG_VORPOMMERN(
        definition = MecklenburgVorpommern,
        iso3166_2 = "DE-MV",
        deBId = 13
    ),
    SACHSEN(
        definition = Sachsen,
        iso3166_2 = "DE-SN",
        deBId = 14
    ),
    SACHSEN_ANHALT(
        definition = SachsenAnhalt,
        iso3166_2 = "DE-ST",
        deBId = 15
    ),
    THUERINGEN(
        definition = Thueringen,
        iso3166_2 = "DE-TH",
        deBId = 16
    ),
    CUSTOM(
        definition = Custom,
        iso3166_2 = "CUSTOM"
    );

    val iso3166_2_DE: String
        get() = iso3166_2.substringAfter("DE-")

    companion object {
        fun getByIso(iso: String): Bundesland = entries.find { it.iso3166_2 == iso } ?: CUSTOM
        fun getByDEBId(id: Int): Bundesland = entries.find { it.deBId == id } ?: CUSTOM
        fun getByShortIso(countryIso: String, iso: String): Bundesland =
            entries.find { it.iso3166_2.substringAfter("$countryIso-") == iso } ?: CUSTOM
    }
}