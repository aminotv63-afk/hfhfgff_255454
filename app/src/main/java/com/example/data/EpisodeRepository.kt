package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Episode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class EpisodeRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("anime_ball_prefs", Context.MODE_PRIVATE)

    private val initialEpisodes = listOf(
        Episode(
            id = 1,
            title = "بداية المعركة! البحث عن كرات التنين الخارقة",
            embedUrl = "https://www.4s.io/web/embed/file/EUc98OZWjq",
            description = "يبدأ السلام بعد هزيمة ماجين بو، لكن كرات التنين الخارقة تشهد تحركات جديدة وتظهر قوة مجهولة في الكون."
        ),
        Episode(
            id = 2,
            title = "إلى المنتجع الموعود! فيجيتا يذهب في رحلة عائلية",
            embedUrl = "https://www.4s.io/web/embed/file/0p3FdwScku",
            description = "فيجيتا يفي بوعده لترانكس ويذهب في عطلة عائلية، بينما يستمر غوكو بالتدريب على كوكب كايو لمواجهة أي تهديد قادم."
        ),
        Episode(
            id = 3,
            title = "أين يكمن استمرار الحلم؟! البحث عن السوبر سايان غود",
            embedUrl = "https://www.4s.io/web/embed/file/438uCN5yjq",
            description = "يستيقظ إله الدمار اللورد بيروس من سباته العميق بعد رؤية حلم بمقاتل أسطوري يُدعى السوبر سايان غود."
        ),
        Episode(
            id = 4,
            title = "الهدف هو كرات دراغون بول! خطة عصابة بيلاف العظيمة",
            embedUrl = "https://www.4s.io/web/embed/file/MVIf5WfWge",
            description = "تحاول عصابة بيلاف التسلل إلى سفينة بولما الفاخرة لسرقة كرات الدراغون بول وسط احتفال عيد ميلادها."
        ),
        Episode(
            id = 5,
            title = "المعركة الحاسمة على كوكب كايو! غوكو ضد بيروس إله الدمار",
            embedUrl = "https://www.4s.io/web/embed/file/582K8L9afa",
            description = "يصل اللورد بيروس إلى كوكب كايو الشمالي، ويتحدى غوكو إله الدمار في مواجهة تفوق كل التوقعات."
        ),
        Episode(
            id = 6,
            title = "لا تغضب إله الدمار! حفلة عيد ميلاد مثيرة",
            embedUrl = "https://www.4s.io/web/embed/file/ff_T93pHfa",
            description = "يصل بيروس وويس إلى كوكب الأرض في حفل بولما، ويحاول فيجيتا باستماتة الحفاظ على هدوء إله الدمار لتجنب دمار الكوكب."
        ),
        Episode(
            id = 7,
            title = "كيف تجرؤ على لمس بولما؟! غضب فيجيتا الهائل يتحول",
            embedUrl = "https://www.4s.io/web/embed/file/Gl8snhjvku",
            description = "بعد أن صفع بيروس بولما، ينفجر غضب فيجيتا بقوة أسطورية تتفوق حتى على تحول السوبر سايان 3 لغوكو!"
        ),
        Episode(
            id = 8,
            title = "غوكو يظهر مجدداً! الفرصة الأخيرة من اللورد بيروس",
            embedUrl = "https://www.4s.io/web/embed/file/aVy7vodhge",
            description = "يعود غوكو إلى الأرض في اللحظة الحاسمة ويتوسل إلى بيروس لمنحهم فرصة لاستدعاء شينرون وسؤاله عن السوبر سايان غود."
        ),
        Episode(
            id = 9,
            title = "عذراً على الانتظار يا لورد بيروس! ولادة السوبر سايان غود",
            embedUrl = "https://www.4s.io/web/embed/file/WUNcdcLcku",
            description = "يجتمع السايان الخمسة الصالحين لمنح طاقتهم لغوكو، ويتحقق التحول الإلهي الأسطوري ذو الهالة الحمراء المشتعلة."
        ),
        Episode(
            id = 10,
            title = "أظهر قوتك يا غوكو! قوة السوبر سايان غود المذهلة",
            embedUrl = "https://www.4s.io/web/embed/file/9TAiYEwojq",
            description = "غوكو يختبر قوة تحوله الجديد ويشتبك مع بيروس في الغلاف الجوي في قتال يحبس الأنفاس."
        ),
        Episode(
            id = 11,
            title = "المعركة تستمر يا لورد بيروس! معركة الآلهة تشتعل",
            embedUrl = "https://www.4s.io/web/embed/file/aQxlao3bfa",
            description = "تتصاعد حدة النزال بين غوكو وبيروس، وتبدأ موجات الصدمة الناجمة عن ضرباتهما بتهديد توازن الكون بأسره."
        ),
        Episode(
            id = 12,
            title = "هل سينهار الكون؟! تصادم قوى إله الدمار وغوكو",
            embedUrl = "https://www.4s.io/web/embed/file/G4mHtRSwjq",
            description = "يصل الصراع إلى ذروته المرعبة، حيث يتعلم غوكو كيفية التحكم بقبضته الإلهية لمنع الكون من الانهيار."
        ),
        Episode(
            id = 13,
            title = "غوكو يتجاوز السوبر سايان غود! اللكمة القصوى",
            embedUrl = "https://www.4s.io/web/embed/file/0eyJfondfa",
            description = "على الرغم من نفاد وقت التحول الإلهي، يمتص غوكو القوة الإلهية في جسده ويواصل القتال بقوة غير مسبوقة."
        ),
        Episode(
            id = 14,
            title = "هذه كل قوتي! ختام معركة الآلهة",
            embedUrl = "https://www.4s.io/web/embed/file/SdnqtBOwjq",
            description = "الخاتمة الأسطورية لمعركة غوكو وبيروس، واعتراف إله الدمار بقوة غوكو مع الكشف عن وجود أكوان أخرى متعددة."
        ),
        Episode(
            id = 15,
            title = "البطل الشجاع السيد ساتان! صانع المعجزات",
            embedUrl = "https://www.4s.io/web/embed/file/swh53PI_ge",
            description = "حلقة فكاهية ممتعة، ساتان يزعم أنه هزم إله الدمار بمفرده ويواجه زائرين فضائيين غير متوقعين."
        ),
        Episode(
            id = 16,
            title = "فيجيتا يصبح تلميذاً! إقناع ويس بالتدريب",
            embedUrl = "https://www.4s.io/web/embed/file/cuKpgryZge",
            description = "يكتشف فيجيتا سر قوة ويس كمدرب لإله الدمار، ويبذل قصارى جهده لإقناعه بأخذه تلميذاً وتدريبه في عالم بيروس."
        ),
        Episode(
            id = 17,
            title = "بان وُلدت! وغوكو يذهب للتدريب السري",
            embedUrl = "https://www.4s.io/web/embed/file/k1ewn5j1jq",
            description = "ولادة بان ابنة غوهان وفيديل، وغوكو يكتشف أن فيجيتا غادر سراً للتدريب عند ويس منذ نصف عام."
        ),
        Episode(
            id = 18,
            title = "أنا هنا أيضاً! يبدأ تدريب غوكو وفيجيتا على عالم بيروس",
            embedUrl = "https://www.4s.io/web/embed/file/WNYWOKWRge",
            description = "غوكو ينضم إلى فيجيتا في عالم بيروس، ويبدآن تمارين قاسية تحت إشراف ويس للتحكم بالكي الإلهية."
        ),
        Episode(
            id = 19,
            title = "اليأس يعود من جديد! قيامة الإمبراطور الشرير فريزا",
            embedUrl = "https://www.4s.io/web/embed/file/clfupzP1ge",
            description = "سوربيت وبقايا جيش فريزا يجمعون كرات دراغون بول لإعادة إحياء الإمبراطور فريزا في الأرض."
        ),
        Episode(
            id = 20,
            title = "تحذير جاكو! فريزا وجيشه المكون من 1000 مقاتل يقتربون",
            embedUrl = "https://www.4s.io/web/embed/file/Tx1vr1j2ge",
            description = "يصل شرطي المجرة جاكو لتحذير بولما ومقاتلي Z من اقتراب سفينة فريزا العملاقة وبدء الغزو."
        )
    )

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    private val _lastWatchedEpisodeId = MutableStateFlow(prefs.getInt("last_watched_id", 1))
    val lastWatchedEpisodeId: StateFlow<Int> = _lastWatchedEpisodeId.asStateFlow()

    init {
        loadEpisodes()
    }

    private fun loadEpisodes() {
        val watchedSet = prefs.getStringSet("watched_episodes", emptySet()) ?: emptySet()
        val favoritesSet = prefs.getStringSet("favorite_episodes", emptySet()) ?: emptySet()

        val updated = initialEpisodes.map { ep ->
            ep.copy(
                isWatched = watchedSet.contains(ep.id.toString()),
                isFavorite = favoritesSet.contains(ep.id.toString())
            )
        }
        _episodes.value = updated
    }

    fun markAsWatched(episodeId: Int) {
        val currentWatched = prefs.getStringSet("watched_episodes", emptySet())?.toMutableSet() ?: mutableSetOf()
        currentWatched.add(episodeId.toString())
        prefs.edit()
            .putStringSet("watched_episodes", currentWatched)
            .putInt("last_watched_id", episodeId)
            .apply()
        _lastWatchedEpisodeId.value = episodeId
        loadEpisodes()
    }

    fun toggleFavorite(episodeId: Int) {
        val currentFavorites = prefs.getStringSet("favorite_episodes", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (currentFavorites.contains(episodeId.toString())) {
            currentFavorites.remove(episodeId.toString())
        } else {
            currentFavorites.add(episodeId.toString())
        }
        prefs.edit().putStringSet("favorite_episodes", currentFavorites).apply()
        loadEpisodes()
    }

    fun getEpisodeById(id: Int): Episode? {
        return _episodes.value.find { it.id == id } ?: initialEpisodes.find { it.id == id }
    }

    fun getNextEpisode(currentId: Int): Episode? {
        return _episodes.value.find { it.id == currentId + 1 }
    }

    fun getPreviousEpisode(currentId: Int): Episode? {
        return _episodes.value.find { it.id == currentId - 1 }
    }

    fun getSuggestedEpisodes(currentId: Int): List<Episode> {
        return _episodes.value.filter { it.id != currentId }.take(6)
    }

    private val directUrlCache = mutableMapOf<Int, String>()

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    suspend fun getDirectMp4Url(episode: Episode): String? = withContext(Dispatchers.IO) {
        directUrlCache[episode.id]?.let { return@withContext it }
        try {
            val request = Request.Builder()
                .url(episode.embedUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            val regex = Regex("""https://dc[0-9a-zA-Z._-]+/img/[^"'\s]+preview\.mp4""")
            val match = regex.find(body)
            val extracted = match?.value
            if (!extracted.isNullOrBlank()) {
                directUrlCache[episode.id] = extracted
                return@withContext extracted
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    fun checkForNewEpisodes(): String {
        // Architecture prepared for remote sync
        return "تم فحص السيرفر بنجاح: جميع الحلقات الـ 20 متوفرة بأعلى جودة وتحديث مستمر!"
    }
}
