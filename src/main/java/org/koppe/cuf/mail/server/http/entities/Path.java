package org.koppe.cuf.mail.server.http.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;
import lombok.Getter;

/**
 * Represents an {@link org.koppe.cuf.mail.server.http.entities.Endpoint} path
 */
public interface Path {
    /**
     * String representation of the path of the endpoint
     * 
     * @return String representation of the path of the endpoint
     */
    public String getPath();

    /**
     * Arguments the path holds
     * 
     * @return Arguments the path holds
     */
    public Map<String, String> getArguments();

    /**
     * Returns true, if this path matches the given string
     * 
     * @param path Path to check for
     * @return True, if given path amtches this string
     */
    public boolean matches(String path);

    public boolean equals(Path other);

    // #region static
    /**
     * List of all regexes with which to check, if given Strings match the path
     */
    static final Map<Path, String> regexes = new HashMap<>();
    static final Map<Path, Map<Integer, PlaceHolder>> placeHolders = new HashMap<>();

    // #region s_matches
    /**
     * Static implementation that can be used to match Paths initialized by
     * {@link Path#of(String, Map)} against Strings
     * 
     * @param path     Path check for matches
     * @param resource Given resource to check against the path
     * @return True, if path matches
     * @throws IllegalArgumentException
     */
    static boolean s_matches(Path path, String resource) throws IllegalArgumentException {
        if (regexes.containsKey(path))
            return resource.matches(regexes.get(path));

        throw new IllegalArgumentException();
    }

    // #region initialize
    /**
     * Initializes the path as a regex in the regex registry to match against
     * 
     * @param path Path to initialize
     * @throws IllegalArgumentException
     */
    static void initialize(Path path) throws IllegalArgumentException {
        if (regexes.containsKey(path))
            return;

        String regex = "^";
        boolean initPlaceholder = false;
        String placeHolder = "";
        int currentGroup = 0;

        for (int i = 0; i < path.getPath().length(); i++) {
            String current = "" + path.getPath().charAt(i);

            if (current.equals("{")) {
                if (path.getArguments() == null)
                    throw new IllegalArgumentException("Cannot initialize place holders without argument definitions");

                if (initPlaceholder)
                    throw new IllegalArgumentException("Second place holder initializer found, invalid path");

                regex += "(";
                initPlaceholder = true;
                continue;
            }

            if (current.equals("}")) {
                if (!initPlaceholder)
                    throw new IllegalArgumentException("Second place holder terminator found, invalid path");

                regex += switch (path.getArguments().get(placeHolder)) {
                    case "String" -> ".+";
                    case "Integer" -> "\\d+";
                    case "Long", "Float" -> "\\d+[,.]{1}\\d+";
                    default -> throw new IllegalArgumentException("Missing placeholder definition");
                };

                if (!placeHolders.containsKey(path))
                    placeHolders.put(path, new HashMap<>());

                placeHolders.get(path).put(currentGroup, new PlaceHolder(placeHolder,
                        currentGroup, path.getArguments().get(placeHolder)));
                currentGroup++;
                initPlaceholder = false;
                placeHolder = "";
                regex += ")";
                continue;
            }

            if (initPlaceholder) {
                if (current.equals("/"))
                    throw new IllegalArgumentException("Placeholder not terminated");
                placeHolder += current;
                continue;
            }

            if (current.equals("\\"))
                current = "\\\\";
            regex += current;
        }

        if (initPlaceholder) {
            throw new IllegalArgumentException("Placeholder not terminated");
        }

        regex += "$";
        regexes.put(path, regex);
    }

    public static Map<String, Object> getArguments(String resource, Path path) {
        Map<String, Object> args = new HashMap<>();

        if (!regexes.containsKey(path))
            initialize(path);

        Pattern pattern = Pattern.compile(regexes.get(path));
        Matcher matcher = pattern.matcher(resource);

        if (!matcher.find())
            return null;

        for (int i = 1; i <= matcher.groupCount(); i++) {
            String arg = matcher.group(i);
            String type = placeHolders.get(path).get(i - 1).getType();
            switch (type) {
                case "Integer":
                    args.put(placeHolders.get(path).get(i - 1).getName(), Integer.parseInt(arg));
                    break;
                case "Float":
                    args.put(placeHolders.get(path).get(i - 1).getName(), Float.parseFloat(arg));
                    break;
                case "Long":
                    args.put(placeHolders.get(path).get(i - 1).getName(), Long.parseLong(arg));
                    break;
                default:
                    args.put(placeHolders.get(path).get(i - 1).getName(), arg);
                    break;
            }
        }

        return args;
    }

    // #region of
    /**
     * Initializes a new Path object
     * 
     * @param p    Path to be initialized
     * @param args Arguments in the path.
     * @return The initialized path.
     */
    public static Path of(String p, Map<String, String> args) {
        Path pt = new Path() {
            @Getter
            private final String path = p;
            @Getter
            private final Map<String, String> arguments = args;

            @Override
            public boolean matches(String path) {
                return Path.s_matches(this, path);
            }

            @Override
            public String toString() {
                return new StringBuilder()
                        .append("Path(")
                        .append("path=")
                        .append(p)
                        .append(", arguments=")
                        .append(arguments)
                        .append(")")
                        .toString();
            }

            @Override
            public boolean equals(Path other) {
                return other.getPath().equals(this.path);
            }

            @Override
            public int hashCode() {
                return path.hashCode();
            }
        };

        initialize(pt);
        return pt;
    }

    @Data
    static class PlaceHolder {
        private final String name;
        private final int position;
        private final String type;
    }
}
