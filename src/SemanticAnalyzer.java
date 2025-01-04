package snake;

import java.util.HashMap;
import java.util.Map;

public class SemanticAnalyzer {
    private Map<String, String> declaredVariables = new HashMap<>();
    private Map<String, Object> variableValues = new HashMap<>();

    public String analyze(String code) {
        StringBuilder result = new StringBuilder("Résultat de l'analyse sémantique :\n");
        String[] lines = code.split("\n");

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty() || isIgnoredLine(trimmedLine)) continue;

            if (trimmedLine.startsWith("Snk_Int") || trimmedLine.startsWith("Snk_Real") || trimmedLine.startsWith("Snk_Strg")) {
                processDeclaration(trimmedLine, result);
            } else if (trimmedLine.startsWith("Set")) {
                processAssignment(trimmedLine, result);
            } else if (trimmedLine.startsWith("Get")) {
                processGet(trimmedLine, result);
            } else if (trimmedLine.startsWith("Snk_Print")) {
                processPrint(trimmedLine, result);
            } else if (isArithmeticExpression(trimmedLine)) {
                processArithmetic(trimmedLine, result);
            } else {
                result.append("Erreur : Ligne non reconnue ou syntaxe incorrecte : ").append(trimmedLine).append("\n");
            }
        }

        return result.toString();
    }

    private boolean isIgnoredLine(String line) {
        return line.startsWith("Snk_Begin") || line.startsWith("Snk_End") ||
               line.startsWith("If") || line.startsWith("Else") ||
               line.startsWith("Begin") || line.startsWith("End") ||
               line.startsWith("##");
    }

    private void processDeclaration(String line, StringBuilder result) {
        String type = line.split(" ")[0].replace("Snk_", "").toLowerCase();
        String[] variables = line.substring(line.indexOf(' ') + 1).replace("#", "").split(",");

        for (String variable : variables) {
            variable = variable.trim();
            if (!variable.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                result.append("Erreur : Identifiant invalide : ").append(variable).append("\n");
            } else if (declaredVariables.containsKey(variable)) {
                result.append("Erreur : La variable '").append(variable).append("' est déjà déclarée.\n");
            } else {
                declaredVariables.put(variable, type);
                result.append("Déclaration correcte : ").append(variable).append(" de type ").append(type).append("\n");
            }
        }
    }

    private void processAssignment(String line, StringBuilder result) {
        String[] parts = line.split("\s+", 3);
        if (parts.length < 3 || !line.endsWith("#")) {
            result.append("Erreur : Affectation incorrecte. Ligne : ").append(line).append("\n");
            return;
        }

        String variable = parts[1];
        String expression = parts[2].replace("#", "").trim();

        if (!declaredVariables.containsKey(variable)) {
            result.append("Erreur : La variable '").append(variable).append("' n'est pas déclarée.\n");
            return;
        }

        try {
            double resultValue = evaluateExpression(expression);
            variableValues.put(variable, resultValue);
            result.append("Affectation correcte : ").append(variable).append(" = ").append(resultValue).append("\n");
        } catch (Exception e) {
            result.append("Erreur : Expression arithmétique invalide dans l'affectation : ").append(line).append("\n");
        }
    }

    private void processGet(String line, StringBuilder result) {
        String[] parts = line.split("\s+");
        if (parts.length < 4 || !"from".equals(parts[2]) || !line.endsWith("#")) {
            result.append("Erreur : Instruction Get incorrecte. Ligne : ").append(line).append("\n");
            return;
        }

        String var1 = parts[1];
        String var2 = parts[3].replace("#", "");

        if (!declaredVariables.containsKey(var1) || !declaredVariables.containsKey(var2)) {
            result.append("Erreur : Variables utilisées non déclarées dans Get. Ligne : ").append(line).append("\n");
        } else {
            result.append("Instruction Get correcte : ").append(line).append("\n");
        }
    }

    private void processPrint(String line, StringBuilder result) {
        String content = line.substring(line.indexOf(" ") + 1).replace("#", "").trim();

        if (content.startsWith("\"") && content.endsWith("\"")) {
            result.append("Affichage de chaîne : ").append(content).append("\n");
        } else {
            for (String variable : content.split(",")) {
                variable = variable.trim();
                if (declaredVariables.containsKey(variable)) {
                    result.append(variable).append(" = ").append(variableValues.getOrDefault(variable, "non initialisé")).append(" ");
                } else {
                    result.append("Erreur : La variable '").append(variable).append("' n'est pas déclarée.\n");
                }
            }
            result.append("\n");
        }
    }

    private void processArithmetic(String line, StringBuilder result) {
        try {
            String expression = line.trim(); // Utilise la ligne entière comme expression
            double value = evaluateExpression(expression);
            result.append("Résultat de l'expression : ").append(expression).append(" = ").append(value).append("\n");
        } catch (Exception e) {
            result.append("Erreur : Expression arithmétique invalide : ").append(line).append("\n");
        }
    }

    private boolean isArithmeticExpression(String expression) {
        // Identifie uniquement les expressions arithmétiques valides
        return expression.matches(".*[\\d\\+\\-\\*/\\^()]+.*");
    }

    private double evaluateExpression(String expression) {
        // Évalue une expression mathématique via une analyse syntaxique récursive
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
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
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor());
                return x;
            }
        }.parse();
    }
}
