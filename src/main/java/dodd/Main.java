package dodd;

import java.util.*;
import java.util.function.*;
import java.util.stream.IntStream;

public class Main {

    static class Box<T> {

        T value;

        Box(T value) {
            this.value = value;
        }

        public String toString() {
            return value.toString();
        }
    }

    record Ball(int runs, boolean wicket) {}

    static int mod(int x, int y) {
        return (x % y + y) % y;
    }

    static int strHash(String str) {
        return str.chars().reduce(5381, (acc, x) -> 127 * (acc & 0x00FFFFFF) + x);
    }

    static Iterable<Integer> filteredStr(String str) {
        return () -> str.codePoints().filter(x -> x < 0xFE00 || x > 0xFE0F).iterator();
    }

    static class Team {

        final String name;

        final String coachUpper;
        final String nameUpper;

        final List<String> players = new ArrayList<>();
        final List<String> playersLast = new ArrayList<>();

        final List<Integer> bowl = new ArrayList<>();
        final List<Integer> wk = new ArrayList<>();

        final int uuid;

        Team(String coach, String name, List<String> players) {
            this.name = name;

            coachUpper = coach.toUpperCase(Locale.ROOT);
            nameUpper = name.toUpperCase(Locale.ROOT);

            uuid = strHash(coach + name);

            for (int i = 0; i < 11; ++i) {
                var player = players.get(i);
                var isWk = false;
                var last = "";
                var split = player.split("\\s+");
                for (int j = split.length - 1; j >= 0; --j) {
                    var word = split[j];
                    var lower = word.toLowerCase(Locale.ROOT);
                    if (lower.equals("(wk)") || lower.equals("[wk]") || lower.equals("{wk}")) {
                        isWk = true;
                    }
                    else if (!lower.equals("(c)") && !lower.equals("[c]") && !lower.equals("{c}")) {
                        last = word;
                        break;
                    }
                }
                this.players.add(player);
                this.playersLast.add(last);
                if (isWk) {
                    this.wk.add(i);
                }
                else if (i - wk.size() > 5) {
                    this.bowl.add(i);
                }
            }
        }

        String bowler(int count) {
            var test = (count / 6) * this.uuid;
            if (test % 8 == 0) {
                var index = mod(test, 11);
                if (!this.wk.contains(index)) {
                    return this.playersLast.get(index);
                }
            }
            return this.playersLast.get(this.bowl.get(mod(test, this.bowl.size())));
        }
    }

    public static void main(String[] args) {
        final var scanner = new Scanner(System.in);
        
        Supplier<Team> parseTeam = () -> {
            var coach = scanner.nextLine();
            var name = scanner.nextLine();
            scanner.nextLine();
            var players = IntStream.range(0, 11).mapToObj(x -> scanner.nextLine()).toList();
            scanner.nextLine();
            return new Team(coach, name, players);
        };

        IntFunction<String> runsStr = x -> x == 0 ? "🦆" : Integer.toString(x);

        final var teams = new Team[] {parseTeam.get(), parseTeam.get()};
        final var teamRuns = new int[] {0, 0};

        final var toss = Integer.parseInt(scanner.nextLine()) - 1;
        final var batting = new Box<>(0);
        final var line = scanner.nextLine().toLowerCase(Locale.ROOT);
        batting.value = line.equals("bat") || line.equals("batting") ? toss : 1 - toss;
        scanner.nextLine();

        IntSupplier bowling = () -> 1 - batting.value;

        final var count = new Box<>(6 * 123456789);

        final var batterRuns = new Box<>(0);
        // final var batterBalls = new Box<>(0);
        final var totalRuns = new Box<>(0);
        final var totalWickets = new Box<>(0);
        final var wicket = new Box<>(false);
        final var innings = new Box<>(1);
        final var target = new Box<>(0);

        IntFunction<String> runPl = x -> x + " RUN" + (x == 1 ? "" : "S");

        IntFunction<String> wicketPl = x -> x + " WICKET" + (x == 1 ? "" : "S");

        Supplier<String> batter = () -> teams[batting.value].players.get(totalWickets.value);

        Supplier<String> bowler = () -> teams[bowling.getAsInt()].bowler(count.value);

        Consumer<Boolean> printBatter = x -> {
            if (x) {
                System.out.println(batter.get() + " " + runsStr.apply(batterRuns.value) + " (" + bowler.get() + ")");
            } else {
                System.out.println(batter.get() + " " + batterRuns + " n/o");
            }
        };

        Runnable printCurrentInnings = () -> {
            printBatter.accept(false);
            for (var i = 1 + totalWickets.value; i < 11; ++i){
                System.out.println(teams[batting.value].players.get(i));
            }
            System.out.println("\n" + totalRuns + "/" + totalWickets + "\n");
        };

        Map<Integer, Ball> ballDict = new HashMap<>();

        BiConsumer<Ball, int[]> putBall = (x, y) -> {
            for (var code : y) {
                ballDict.put(code, x);
            }
        };

        var gone = new Ball(0, true);
        var dot = new Ball(0, false);
        var one = new Ball(1, false);
        var two = new Ball(2, false);
        var three = new Ball(3, false);
        var four = new Ball(4, false);
        var five = new Ball(5, false);
        var six = new Ball(6, false);

        putBall.accept(gone, new int[] {33, 10084, 128308, 128997});
        putBall.accept(dot, new int[] {48, 128420, 9899, 11035, 9899, 11035});
        putBall.accept(one, new int[] {49, 129293, 9898, 11036, 9898, 11036});
        putBall.accept(two, new int[] {50, 128154, 128994, 129001});
        putBall.accept(three, new int[] {51, 129505, 128992, 128999});
        putBall.accept(four, new int[] {52, 128153, 128309, 128998});
        putBall.accept(five, new int[] {53, 128156, 128995, 129002});
        putBall.accept(six, new int[] {54, 128155, 128993, 129000});

        Supplier<Ball> otherBall = () -> five;

        Function<Integer, Ball> carBall = x -> ballDict.getOrDefault(x, otherBall.get());

        var cars = scanner.nextLine();

        System.out.println(teams[0].coachUpper + " VS " + teams[1].coachUpper + " TEST\n");

        System.out.println(teams[0].nameUpper + "\nvs\n" + teams[1].nameUpper + "\n");

        var decision = toss == batting.value ? "bat" : "bowl";
        System.out.println(teams[toss].name + " won the toss and chose to " + decision + "\n");

        System.out.println("1ST INNINGS\n");

        for (var car : filteredStr(cars)) {
            count.value += 1;
            var ball = carBall.apply(car);
            batterRuns.value += ball.runs;
            // batterBalls.value += 1;
            totalRuns.value += ball.runs;
            teamRuns[batting.value] += ball.runs;
            wicket.value = ball.wicket;
            if (wicket.value) {
                printBatter.accept(true);
                batterRuns.value = 0;
                // batterBalls.value = 0;
                totalWickets.value += 1;
                if (totalWickets.value > 10) {
                    System.out.println("\n" + totalRuns + "/" + totalWickets + "\n");
                    batting.value = bowling.getAsInt();
                    totalRuns.value = 0;
                    totalWickets.value = 0;
                    innings.value += 1;
                    if (innings.value == 3) {
                        var lead = teamRuns[0] - teamRuns[1];
                        if (lead == 0) {
                            System.out.println("SCORES LEVEL");
                        } else {
                            var leading = lead > 0 ? 0 : 1;
                            System.out.println(teams[leading].nameUpper + " LEAD BY " + runPl.apply(Math.abs(lead)) + "\n");
                        }
                        System.out.println("2ND INNINGS\n");
                    } else if (innings.value == 4) {
                        var deficit = teamRuns[bowling.getAsInt()] - teamRuns[batting.value];
                        if (deficit < 0) {
                            System.out.println(teams[batting.value].nameUpper + " WIN BY AN INNINGS AND " + runPl.apply(-deficit));
                            return;
                        } else {
                            target.value = 1 + deficit;
                            System.out.println(teams[batting.value].nameUpper + " NEED " + runPl.apply(target.value) + " TO WIN\n");
                        }
                    } else if (innings.value > 4) {
                        var lead = teamRuns[batting.value] - teamRuns[bowling.getAsInt()];
                        if (lead == 0) {
                            System.out.println("MATCH TIED\n");
                        } else {
                            System.out.println(teams[batting.value].nameUpper + " WIN BY " + runPl.apply(lead) + "\n");
                        }
                        return;
                    }
                }
            } else if (innings.value == 4 && totalRuns.value >= target.value) {
                printCurrentInnings.run();
                System.out.println(teams[batting.value].nameUpper + " WIN BY " + wicketPl.apply(11 - totalWickets.value));
                return;
            }
        }

        printCurrentInnings.run();

        var lead = teamRuns[batting.value] - teamRuns[bowling.getAsInt()];
        if (innings.value == 4) {
            System.out.println(teams[batting.value].nameUpper + " NEED " + runPl.apply(1 - lead) + " TO WIN");
        } else {
            if (lead == 0) {
                System.out.println("SCORES LEVEL");
            } else {
                var leading = lead > 0 ? batting.value : bowling.getAsInt();
                System.out.println(teams[leading].nameUpper + " LEAD BY " + runPl.apply(Math.abs(lead)));
            }
        }
    }
}
