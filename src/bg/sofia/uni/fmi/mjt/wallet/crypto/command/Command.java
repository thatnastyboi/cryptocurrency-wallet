package bg.sofia.uni.fmi.mjt.wallet.crypto.command;

public record Command(String command, String... arguments) {

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(command).append(" ");

        for (String s : arguments) {
            result.append(s).append(" ");
        }

        return result.toString();
    }
}
