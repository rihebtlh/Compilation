import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SemanticAnalyzer {
    // Variables déclarées (nom -> type : int / real / strg) et leurs valeurs
    private final Map<String, String> declaredVariables = new HashMap<>();
    private final Map<String, Object> variableValues = new HashMap<>();

    private static final Set<String> IGNORED_KEYWORDS =
            Set.of("Snk_Begin", "Snk_End", "If", "Else", "Begin", "End");

    public String analyze(String code) {
        // Réinitialiser l'état : sinon une 2e analyse signale "déjà déclarée"
        declaredVariables.clear();
        variableValues.clear();

        StringBuilder result = new StringBuilder("Resultat de l'analyse semantique :\n");

        for (String rawLine : code.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("##")) continue;

            // Mot-clé exact (et non startsWith : "Settings" ne doit pas être un "Set")
            String keyword = line.split("[\\s(#]", 2)[0];
            if (IGNORED_KEYWORDS.contains(keyword)) continue;

            switch (keyword) {
                case "Snk_Int":
                case "Snk_Real":
                case "Snk_Strg":
                    processDeclaration(line, result);
                    break;
                case "Set":
                    processAssignment(line, result);
                    break;
                case "Get":
                    processGet(line, result);
                    break;
                case "Snk_Print":
                    processPrint(line, result);
                    break;
                default:
                    if (isArithmeticExpression(line)) {
                        processArithmetic(line, result);
                    } else {
                        result.append("Erreur : Ligne non reconnue ou syntaxe incorrecte : ")
                              .append(line).append("\n");
                    }
            }
        }
        return result.toString();
    }

    // ---------- Utilitaires ----------

    /** Retire uniquement le '#' final (et non tous les '#', ce qui cassait les chaînes). */
    private String stripHash(String line) {
        String s = line.trim();
        if (s.endsWith("#")) s = s.substring(0, s.length() - 1);
        return s.trim();
    }

    private String format(Object value) {
        if (value instanceof Double) {
            double d = (Double) value;
            if (d == Math.rint(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15) {
                return String.valueOf((long) d);
            }
        }
        return String.valueOf(value);
    }

    // ---------- Instructions ----------

    private void processDeclaration(String line, StringBuilder result) {
        if (!line.endsWith("#")) {
            result.append("Erreur : '#' manquant en fin de déclaration : ").append(line).append("\n");
            return;
        }
        String[] parts = stripHash(line).split("\\s+", 2);
        String type = parts[0].substring("Snk_".length()).toLowerCase();

        if (parts.length < 2 || parts[1].isBlank()) {
            result.append("Erreur : Aucune variable dans la déclaration : ").append(line).append("\n");
            return;
        }

        for (String variable : parts[1].split(",", -1)) {
            variable = variable.trim();
            if (!variable.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                result.append("Erreur : Identifiant invalide : '").append(variable).append("'\n");
            } else if (declaredVariables.containsKey(variable)) {
                result.append("Erreur : La variable '").append(variable).append("' est déjà déclarée.\n");
            } else {
                declaredVariables.put(variable, type);
                result.append("Déclaration correcte : ").append(variable)
                      .append(" de type ").append(type).append("\n");
            }
        }
    }

    private void processAssignment(String line, StringBuilder result) {
        if (!line.endsWith("#")) {
            result.append("Erreur : Affectation incorrecte ('#' manquant). Ligne : ").append(line).append("\n");
            return;
        }
        String[] parts = stripHash(line).split("\\s+", 3); // était "\s+" => erreur de compilation
        if (parts.length < 3) {
            result.append("Erreur : Affectation incorrecte. Ligne : ").append(line).append("\n");
            return;
        }

        String variable = parts[1];
        String expression = parts[2].trim();

        if (!declaredVariables.containsKey(variable)) {
            result.append("Erreur : La variable '").append(variable).append("' n'est pas declaree.\n");
            return;
        }

        String type = declaredVariables.get(variable);
        try {
            Object value;
            if (type.equals("strg")) {
                if (expression.length() < 2 || !expression.startsWith("\"") || !expression.endsWith("\"")) {
                    throw new IllegalArgumentException("une chaîne entre guillemets est attendue pour '" + variable + "'");
                }
                value = expression;
            } else {
                double d = evaluateExpression(expression);
                if (type.equals("int")) {
                    if (Double.isInfinite(d) || Double.isNaN(d) || d != Math.rint(d)) {
                        throw new IllegalArgumentException("valeur non entière affectée à la variable int '" + variable + "'");
                    }
                    value = (long) d;
                } else {
                    value = d;
                }
            }
            variableValues.put(variable, value);
            result.append("Affectation correcte : ").append(variable).append(" = ").append(value).append("\n");
        } catch (RuntimeException e) {
            result.append("Erreur : ").append(e.getMessage()).append(". Ligne : ").append(line).append("\n");
        }
    }

    private void processGet(String line, StringBuilder result) {
        String[] parts = stripHash(line).split("\\s+"); // était "\s+" => erreur de compilation
        if (!line.endsWith("#") || parts.length != 4 || !"from".equals(parts[2])) {
            result.append("Erreur : Instruction Get incorrecte. Ligne : ").append(line).append("\n");
            return;
        }

        String var1 = parts[1];
        String var2 = parts[3];

        if (!declaredVariables.containsKey(var1) || !declaredVariables.containsKey(var2)) {
            result.append("Erreur : Variables utilisees non declarees dans Get. Ligne : ").append(line).append("\n");
        } else {
            result.append("Instruction Get correcte : ").append(line).append("\n");
        }
    }

    private void processPrint(String line, StringBuilder result) {
        if (!line.endsWith("#")) {
            result.append("Erreur : '#' manquant en fin d'instruction d'affichage : ").append(line).append("\n");
            return;
        }
        String[] parts = stripHash(line).split("\\s+", 2);
        if (parts.length < 2 || parts[1].isBlank()) {
            result.append("Erreur : Snk_Print sans argument : ").append(line).append("\n");
            return;
        }
        String content = parts[1].trim();

        if (content.length() >= 2 && content.startsWith("\"") && content.endsWith("\"")) {
            result.append("Affichage de chaîne : ").append(content).append("\n");
        } else {
            for (String variable : content.split(",")) {
                variable = variable.trim();
                if (declaredVariables.containsKey(variable)) {
                    result.append(variable).append(" = ")
                          .append(variableValues.containsKey(variable) ? format(variableValues.get(variable)) : "non initialise")
                          .append(" ");
                } else {
                    result.append("Erreur : La variable '").append(variable).append("' n'est pas declaree.\n");
                }
            }
            result.append("\n");
        }
    }

    private void processArithmetic(String line, StringBuilder result) {
        String expression = stripHash(line);
        try {
            double value = evaluateExpression(expression);
            result.append("Resultat de l'expression : ").append(expression)
                  .append(" = ").append(format(value)).append("\n");
        } catch (RuntimeException e) {
            result.append("Erreur : Expression arithmétique invalide (").append(e.getMessage())
                  .append(") : ").append(line).append("\n");
        }
    }

    /** Ligne composée uniquement de chiffres, identifiants, opérateurs et parenthèses. */
    private boolean isArithmeticExpression(String line) {
        return line.matches("[\\w\\s+\\-*/^().]+#?") && line.matches(".*[\\d+\\-*/^].*");
    }

    // ---------- Évaluateur d'expressions ----------

    private double evaluateExpression(String expression) {
        return new ExpressionParser(expression).parse();
    }

    private class ExpressionParser {
        private final String s;
        private int pos = -1, ch;

        ExpressionParser(String s) {
            this.s = s;
        }

        void nextChar() {
            ch = (++pos < s.length()) ? s.charAt(pos) : -1;
        }

        boolean eat(int charToEat) {
            while (ch != -1 && Character.isWhitespace(ch)) nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        double parse() {
            nextChar();
            double x = parseExpression();
            while (ch != -1 && Character.isWhitespace(ch)) nextChar(); // espaces finaux tolérés
            if (pos < s.length()) throw new RuntimeException("caractère inattendu '" + (char) ch + "'");
            return x;
        }

        double parseExpression() {
            double x = parseTerm();
            for (;;) {
                if (eat('+')) x += parseTerm();
                else if (eat('-')) x -= parseTerm();
                else return x;
            }
        }

        double parseTerm() {
            double x = parseFactor();
            for (;;) {
                if (eat('*')) {
                    x *= parseFactor();
                } else if (eat('/')) {
                    double divisor = parseFactor();
                    if (divisor == 0) throw new ArithmeticException("division par zéro");
                    x /= divisor;
                } else {
                    return x;
                }
            }
        }

        double parseFactor() {
            if (eat('+')) return parseFactor();
            if (eat('-')) return -parseFactor();

            double x;
            int startPos = this.pos;
            if (eat('(')) {
                x = parseExpression();
                if (!eat(')')) throw new RuntimeException("parenthèse fermante manquante");
            } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                try {
                    x = Double.parseDouble(s.substring(startPos, this.pos));
                } catch (NumberFormatException e) {
                    throw new RuntimeException("nombre invalide '" + s.substring(startPos, this.pos) + "'");
                }
            } else if (Character.isLetter(ch) || ch == '_') {
                // Les variables sont maintenant gérées dans les expressions
                while (Character.isLetterOrDigit(ch) || ch == '_') nextChar();
                x = valueOf(s.substring(startPos, this.pos));
            } else {
                throw new RuntimeException(ch == -1 ? "fin d'expression inattendue"
                                                    : "caractère inattendu '" + (char) ch + "'");
            }

            if (eat('^')) x = Math.pow(x, parseFactor());
            return x;
        }

        private double valueOf(String name) {
            String type = declaredVariables.get(name);
            if (type == null) throw new RuntimeException("variable '" + name + "' non déclarée");
            if (type.equals("strg")) throw new RuntimeException("variable chaîne '" + name + "' utilisée dans un calcul");
            Object v = variableValues.get(name);
            if (v == null) throw new RuntimeException("variable '" + name + "' non initialisée");
            return ((Number) v).doubleValue();
        }
    }
}
