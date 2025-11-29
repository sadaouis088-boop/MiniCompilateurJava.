/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package analyseur;

/**
 *
 * @author sadao
 */



import java.util.Scanner;

public class AnalyseurLexical {

    // Fonction qui détermine la "colonne" d'un caractère pour notre analyse
    static int col(char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) return 0; // lettre
        else if (c >= '0' && c <= '9') return 1; // chiffre
        else if (c == '_') return 2; // underscore
        else if ("+-*/=<>!&|%".indexOf(c) != -1) return 3; // opérateurs
        else if (";,(){}[].:?".indexOf(c) != -1) return 4; // séparateurs
        else if (c == '"') return 5; // début/fin chaîne de caractères
        else if (c == '\'') return 6; // début/fin caractère
        else if (c == '/') return 7; // début commentaire
        else if (c == ' ' || c == '\t' || c == '\n' || c == '\r') return 8; // espace, tab, retour ligne
        else if (c == '.') return 9; // point pour flottant
        else return -1; // caractère non reconnu
    }

    // Fonction pour afficher un lexème et déterminer s'il est un mot-clé ou identificateur
    static void afficherLexeme(String lexeme, String[] motsCles) {
        boolean estMotCle = false;
        for (String mot : motsCles) {
            if (lexeme.equals(mot)) {
                System.out.println("Mot-clé : " + lexeme);
                estMotCle = true;
                break;
            }
        }
        if (!estMotCle) {
            if (lexeme.matches("[a-zA-Z_][a-zA-Z0-9_]*")) 
                System.out.println("Identificateur : " + lexeme);
            else 
                System.out.println("Lexème : " + lexeme);
        }
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        String code = ""; // variable qui va contenir tout le code à analyser
        String ligne;

        System.out.println("Entrer le programme (termine par # sur une ligne seule) :");

        // Lire toutes les lignes jusqu'à ce que l'utilisateur entre #
        while (!(ligne = input.nextLine()).equals("#")) {
            code = code + ligne + "\n"; // concaténer la ligne + saut de ligne
        }
        code = code + "#"; // ajouter # pour marquer la fin

        // Définir les mots-clés connus du langage Java
         String[] motsCles = {
            "int", "float", "double", "char", "boolean", "String",
            "do", "while", "for", "each", "if", "else",
            "try", "catch", "finally", "switch", "case", "default",
            "break", "continue", "return", "new", "this", "super",
            "public", "private", "protected", "static", "void",
            "class", "interface", "enum", "extends", "implements",
            "import", "package", "instanceof", "synchronized", "throw", "throws"
        };

        String lexeme = ""; // pour stocker le lexème courant
        int i = 0;          // index de lecture dans le code
        char Tc = code.charAt(i); // caractère courant

        // Boucle principale : lire caractère par caractère
        while (Tc != '#') {
            int c = col(Tc); // déterminer la catégorie du caractère

            if (c == -1) { // caractère inconnu
                System.err.println("Erreur lexicale sur le caractère : " + Tc);
                lexeme = "";
                i++;
                Tc = code.charAt(i);
                continue;
            }

            // Lettres et underscore → construction du lexème
            if (c == 0 || c == 2) {
                lexeme += Tc;
                i++;
                Tc = code.charAt(i);
                continue;
            }

            // Chiffres → nombres entiers ou flottants
            if (c == 1) {
                lexeme += Tc;
                i++;
                Tc = code.charAt(i);
                boolean estFloat = false;
                while (col(Tc) == 1 || Tc == '.') { // lire tous les chiffres et au plus un point
                    if (Tc == '.') estFloat = true;
                    lexeme += Tc;
                    i++;
                    Tc = code.charAt(i);
                }
                if (estFloat) System.out.println("Nombre flottant : " + lexeme);
                else System.out.println("Nombre entier : " + lexeme);
                lexeme = "";
                continue;
            }

            // Chaîne de caractères
            if (c == 5) {
                lexeme = "\""; 
                i++; Tc = code.charAt(i);
                while (Tc != '"' && Tc != '#') { // lire jusqu'au prochain guillemet
                    lexeme += Tc; i++; Tc = code.charAt(i);
                }
                if (Tc == '"') {
                    lexeme += '"';
                    System.out.println("Chaîne de caractères : " + lexeme);
                    i++; Tc = code.charAt(i); lexeme = "";
                } else System.err.println("Erreur : chaîne non fermée !");
                continue;
            }

            // Caractère simple
            if (c == 6) {
                lexeme = "'";
                i++; Tc = code.charAt(i);
                while (Tc != '\'' && Tc != '#') { lexeme += Tc; i++; Tc = code.charAt(i); }
                if (Tc == '\'') { lexeme += '\''; System.out.println("Caractère : " + lexeme); i++; Tc = code.charAt(i); lexeme = ""; }
                else System.err.println("Erreur : caractère non fermé !");
                continue;
            }

            // Commentaires
            if (c == 7) {
                i++; Tc = code.charAt(i);
                if (Tc == '/') { // commentaire sur une ligne //
                    i++; Tc = code.charAt(i);
                    while (Tc != '\n' && Tc != '#') { i++; Tc = code.charAt(i); }
                } else if (Tc == '*') { // commentaire multi-lignes /* */
                    i++; Tc = code.charAt(i);
                    while (!(Tc == '*' && code.charAt(i+1) == '/')) { i++; Tc = code.charAt(i); }
                    i += 2; Tc = code.charAt(i);
                }
                lexeme = "";
                continue;
            }

            // Espaces et tabulations
            if (c == 8) {
                if (!lexeme.isEmpty()) { afficherLexeme(lexeme,motsCles); lexeme=""; }
                i++; Tc = code.charAt(i);
                continue;
            }

            // Point pour float
            if (c == 9) { lexeme += Tc; i++; Tc = code.charAt(i); continue; }

            // Opérateurs
            if (c == 3) {
                if (!lexeme.isEmpty()) { afficherLexeme(lexeme,motsCles); lexeme=""; }
                char op1 = Tc;
                char op2 = code.charAt(i+1);
                String doubleOp = "" + op1 + op2;
                // vérifier les opérateurs doubles connus
                if (doubleOp.equals("==") || doubleOp.equals("!=") || doubleOp.equals("<=") || doubleOp.equals(">=") ||
                    doubleOp.equals("++") || doubleOp.equals("--") || doubleOp.equals("+=") || doubleOp.equals("-=") ||
                    doubleOp.equals("*=") || doubleOp.equals("/=") || doubleOp.equals("&&") || doubleOp.equals("||")) {
                    System.out.println("Opérateur : " + doubleOp); i+=2; Tc = code.charAt(i);
                } else {
                    System.out.println("Opérateur : " + op1); i++; Tc = code.charAt(i);
                }
                continue;
            }

            // Séparateurs
               if (c == 4) {
                if (!lexeme.isEmpty()) {
                    afficherLexeme(lexeme, motsCles);
                    lexeme = "";
                }
                if (Tc == '?' || Tc == ':') {
                    System.out.println("Opérateur ternaire : " + Tc);
                } else {
                    System.out.println("Séparateur : " + Tc);
                }
                i++;
                Tc = code.charAt(i);
                continue;
            }

            

            Tc = code.charAt(++i); // avancer au caractère suivant
        }

        if (!lexeme.isEmpty()) afficherLexeme(lexeme,motsCles); // dernier lexème
        System.out.println("FIN DE CHAINE");
    }
}
