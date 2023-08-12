package dodd

import kotlin.math.absoluteValue

// Legacy

/*
val Char.code get() = toInt()

fun String.uppercase() = (this as java.lang.String).toUpperCase(java.util.Locale.ROOT)

fun String.lowercase() = (this as java.lang.String).toLowerCase(java.util.Locale.ROOT)

fun readln() = readLine() ?: throw RuntimeException("EOF reached!")
*/

// Program

typealias Ball = Pair<Int, Boolean>

val whitespaceRegex = Regex("\\s+")

fun mod(x: Int, y: Int) = (x % y + y) % y

fun String.hash() = fold(5381) { acc, x -> 127 * (acc and 0x00FFFFFF) + x.code }

fun String.filtered() = codePoints().filter{ it !in 0xFE00..0xFE0F }

class Team(coach: String, val name: String, players: List<String>) {

    val coachUpper = coach.uppercase()
    val nameUpper = name.uppercase()

    val players = mutableListOf<String>()
    private val playersLast = mutableListOf<String>()

    private val bowl = mutableListOf<Int>()
    private val wk = mutableListOf<Int>()

    private val uuid = (coach + name).hash()

    init {
        for (i in 0 until 11) {
            val player = players[i]
            var isWk = false
            var last = ""
            val split = player.split(whitespaceRegex)
            for (word in split.asReversed()) {
                val lower = word.lowercase()
                if (lower == "(wk)" || lower == "[wk]" || lower == "{wk}") {
                    isWk = true
                }
                else if (lower != "(c)" && lower != "[c]" && lower != "{c}") {
                    last = word
                    break
                }
            }
            this.players.add(player)
            this.playersLast.add(last)
            if (isWk) {
                this.wk.add(i)
            }
            else if (i - wk.size > 5) {
                this.bowl.add(i)
            }
        }
    }

    fun bowler(count: Int): String {
        val test = (count / 6) * this.uuid
        if (test % 8 == 0) {
            val index = mod(test, 11)
            if (!this.wk.contains(index)) {
                return this.playersLast[index]
            }
        }
        return this.playersLast[this.bowl[mod(test, this.bowl.size)]]
    }
}

fun main() {
    fun parseTeam(): Team {
        val coach = readln()
        val name = readln()
        readln()
        val players = (0 until 11).map { readln() }
        readln()
        return Team(coach, name, players)
    }

    fun runsStr(runs: Int) = if (runs == 0) "🦆" else runs.toString()

    val teams = arrayOf(parseTeam(), parseTeam())
    val teamRuns = arrayOf(0, 0)

    val toss = readln().toInt() - 1
    var batting: Int
    val line = readln().lowercase()
    batting = if (line == "bat" || line == "batting") toss else 1 - toss
    readln()

    fun bowling() = 1 - batting

    var count = 6 * 123456789

    var batterRuns = 0
    var batterBalls = 0
    var totalRuns = 0
    var totalWickets = 0
    var wicket: Boolean
    var innings = 1
    var target = 0

    fun runPl(runs: Int) = if (runs == 1) "$runs RUN" else "$runs RUNS"

    fun wicketPl(wickets: Int) = if (wickets == 1) "$wickets WICKET" else "$wickets WICKETS"

    fun batter() = teams[batting].players[totalWickets]

    fun bowler() = teams[bowling()].bowler(count)

    fun printBatter(out: Boolean) {
        if (out) {
            println("${batter()} ${runsStr(batterRuns)} (${bowler()})")
        }
        else {
            println("${batter()} $batterRuns n/o")
        }
    }

    fun printCurrentInnings() {
        printBatter(false)
        for (i in 1 + totalWickets until 11) {
            println(teams[batting].players[i])
        }
        println("\n$totalRuns/$totalWickets\n")
    }

    val ballDict = mutableMapOf<Int, Ball>()

    fun putBall(value: Ball, str: String) {
        for (code in str.filtered()) {
            ballDict[code] = value
        }
    }

    val gone = Ball(0, true)
    val dot = Ball(0, false)
    val one = Ball(1, false)
    val two = Ball(2, false)
    val three = Ball(3, false)
    val four = Ball(4, false)
    val five = Ball(5, false)
    val six = Ball(6, false)

    putBall(gone, "!❤️🔴🟥")
    putBall(dot, "0🖤⚫️⬛️⚫⬛")
    putBall(one, "1🤍⚪️⬜️⚪⬜")
    putBall(two, "2💚🟢🟩")
    putBall(three, "3🧡🟠🟧")
    putBall(four, "4💙🔵🟦")
    putBall(five, "5💜🟣🟪")
    putBall(six, "6💛🟡🟨")

    fun otherBall() = five

    fun carBall(code: Int) = ballDict.getOrDefault(code, otherBall())

    val cars = readln()

    println("${teams[0].coachUpper} VS ${teams[1].coachUpper} TEST\n")

    println("${teams[0].nameUpper}\nvs\n${teams[1].nameUpper}\n")

    val decision = if (toss == batting) "bat" else "bowl"
    println("${teams[toss].name} won the toss and chose to $decision\n")

    println("1ST INNINGS\n")

    for (car in cars.filtered()) {
        count += 1
        val (r, w) = carBall(car)
        batterRuns += r
        batterBalls += 1
        totalRuns += r
        teamRuns[batting] += r
        wicket = w
        if (wicket) {
            printBatter(true)
            batterRuns = 0
            batterBalls = 0
            totalWickets += 1
            if (totalWickets > 10) {
                println("\n$totalRuns/$totalWickets\n")
                batting = bowling()
                totalRuns = 0
                totalWickets = 0
                innings += 1
                if (innings == 3) {
                    val lead = teamRuns[0] - teamRuns[1]
                    if (lead == 0) {
                        println("SCORES LEVEL")
                    }
                    else {
                        val leading = if (lead > 0) 0 else 1
                        println("${teams[leading].nameUpper} LEAD BY ${runPl(lead.absoluteValue)}\n")
                    }
                    println("2ND INNINGS\n")
                }
                else if (innings == 4) {
                    val deficit = teamRuns[bowling()] - teamRuns[batting]
                    if (deficit < 0) {
                        println("${teams[batting].nameUpper} WIN BY AN INNINGS AND ${runPl(-deficit)}")
                        return
                    }
                    else {
                        target = 1 + deficit
                        println("${teams[batting].nameUpper} NEED ${runPl(target)} TO WIN\n")
                    }
                }
                else if (innings > 4) {
                    val lead = teamRuns[batting] - teamRuns[bowling()]
                    if (lead == 0) {
                        println("MATCH TIED\n")
                    }
                    else {
                        println("${teams[batting].nameUpper} WIN BY ${runPl(lead)}\n")
                    }
                    return
                }
            }
        }
        else if (innings == 4 && totalRuns >= target) {
            printCurrentInnings()
            println("${teams[batting].nameUpper} WIN BY ${wicketPl(11 - totalWickets)}")
            return
        }
    }

    printCurrentInnings()

    val lead = teamRuns[batting] - teamRuns[bowling()]
    if (innings == 4) {
        println("${teams[batting].nameUpper} NEED ${runPl(1 - lead)} TO WIN")
    }
    else {
        if (lead == 0) {
            println("SCORES LEVEL")
        }
        else {
            val leading = if (lead > 0) batting else bowling()
            println("${teams[leading].nameUpper} LEAD BY ${runPl(lead.absoluteValue)}")
        }
    }
}
