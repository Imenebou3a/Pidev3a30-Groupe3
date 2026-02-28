package tn.esprit.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import tn.esprit.entities.ProduitLocal;
import tn.esprit.entities.KitHobbies;
import tn.esprit.entities.Favori;

/**
 * Service de recommandations intelligentes utilisant l'API OpenAI GPT
 * Analyse les favoris de l'utilisateur et génère des recommandations personnalisées
 */
public class OpenAIRecommendationService {
    
    private static final String OPENAI_API_KEY = "VOTRE_CLE_API_OPENAI"; // À remplacer
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-3.5-turbo";
    
    private FavoriService favoriService;
    private ProduitLocalService produitService;
    private KitHobbiesService kitService;
    
    public OpenAIRecommendationService() {
        this.favoriService = new FavoriService();
        this.produitService = new ProduitLocalService();
        this.kitService = new KitHobbiesService();
    }
    
    /**
     * Génère des recommandations intelligentes basées sur l'IA OpenAI
     */
    public List<RecommendationIA> genererRecommandationsIA(int idUtilisateur, int limite) {
        try {
            List<Favori> favoris = favoriService.getFavorisByUtilisateur(idUtilisateur);
            
            if (favoris.isEmpty()) {
                System.out.println("⚠️ Aucun favori trouvé, recommandations génériques");
                return genererRecommandationsGeneriques(limite);
            }
            
            String contexte = construireContexteFavoris(favoris);
            String prompt = construirePrompt(contexte, limite);
            String reponseIA = appellerOpenAI(prompt);
            
            return parserRecommandationsIA(reponseIA, idUtilisateur);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur génération recommandations IA: " + e.getMessage());
            e.printStackTrace();
            return genererRecommandationsGeneriques(limite);
        }
    }
    
    private String construireContexteFavoris(List<Favori> favoris) {
        StringBuilder contexte = new StringBuilder();
        contexte.append("Favoris de l'utilisateur:\n");
        
        for (Favori fav : favoris) {
            if ("produit".equals(fav.getTypeFavori())) {
                ProduitLocal produit = produitService.getById(fav.getIdItem());
                if (produit != null) {
                    contexte.append(String.format("- Produit: %s (Catégorie: %s, Région: %s, Prix: %.2f TND)\n",
                        produit.getNom(), produit.getCategorie(), produit.getRegion(), produit.getPrix()));
                }
            } else if ("kit".equals(fav.getTypeFavori())) {
                KitHobbies kit = kitService.getById(fav.getIdItem());
                if (kit != null) {
                    contexte.append(String.format("- Kit: %s (Type: %s, Niveau: %s, Prix: %.2f TND)\n",
                        kit.getNomKit(), kit.getTypeArtisanat(), kit.getNiveauDifficulte(), kit.getPrix()));
                }
            }
        }
        
        return contexte.toString();
    }
    
    private String construirePrompt(String contexte, int limite) {
        return String.format(
            "Tu es un expert en artisanat tunisien et en recommandations personnalisées.\n\n" +
            "%s\n" +
            "Basé sur ces favoris, recommande %d produits ou kits artisanaux similaires.\n" +
            "Pour chaque recommandation, fournis:\n" +
            "1. Le nom du produit/kit\n" +
            "2. La catégorie ou type\n" +
            "3. Un score de pertinence (0-100)\n" +
            "4. Une brève explication (1 phrase)\n\n" +
            "Format de réponse (JSON):\n" +
            "[\n" +
            "  {\"nom\": \"...\", \"type\": \"produit/kit\", \"categorie\": \"...\", \"score\": 95, \"raison\": \"...\"}\n" +
            "]",
            contexte, limite
        );
    }
    
    private String appellerOpenAI(String prompt) throws Exception {
        URL url = new URL(OPENAI_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
        conn.setDoOutput(true);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);
        
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);
        requestBody.put("messages", messages);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject messageObj = choice.getJSONObject("message");
                return messageObj.getString("content");
            }
        } else {
            throw new Exception("Erreur API OpenAI: " + responseCode);
        }
        
        return "[]";
    }
    
    private List<RecommendationIA> parserRecommandationsIA(String reponseIA, int idUtilisateur) {
        List<RecommendationIA> recommandations = new ArrayList<>();
        
        try {
            String jsonStr = reponseIA;
            if (reponseIA.contains("```json")) {
                jsonStr = reponseIA.substring(reponseIA.indexOf("["), reponseIA.lastIndexOf("]") + 1);
            }
            
            JSONArray jsonArray = new JSONArray(jsonStr);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                
                RecommendationIA rec = new RecommendationIA();
                rec.setNom(obj.getString("nom"));
                rec.setType(obj.getString("type"));
                rec.setCategorie(obj.optString("categorie", ""));
                rec.setScore(obj.getInt("score"));
                rec.setRaison(obj.getString("raison"));
                rec.setIdUtilisateur(idUtilisateur);
                
                recommandations.add(rec);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur parsing recommandations IA: " + e.getMessage());
        }
        
        return recommandations;
    }
    
    private List<RecommendationIA> genererRecommandationsGeneriques(int limite) {
        List<RecommendationIA> recommandations = new ArrayList<>();
        
        List<ProduitLocal> produits = produitService.afficher();
        for (int i = 0; i < Math.min(limite, produits.size()); i++) {
            ProduitLocal p = produits.get(i);
            RecommendationIA rec = new RecommendationIA();
            rec.setNom(p.getNom());
            rec.setType("produit");
            rec.setCategorie(p.getCategorie());
            rec.setScore(70);
            rec.setRaison("Produit populaire");
            rec.setIdItem(p.getIdProduit());
            recommandations.add(rec);
        }
        
        return recommandations;
    }
    
    /**
     * Classe interne pour représenter une recommandation IA
     */
    public static class RecommendationIA {
        private int idUtilisateur;
        private int idItem;
        private String nom;
        private String type;
        private String categorie;
        private int score;
        private String raison;
        
        public int getIdUtilisateur() { return idUtilisateur; }
        public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }
        
        public int getIdItem() { return idItem; }
        public void setIdItem(int idItem) { this.idItem = idItem; }
        
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getCategorie() { return categorie; }
        public void setCategorie(String categorie) { this.categorie = categorie; }
        
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        
        public String getRaison() { return raison; }
        public void setRaison(String raison) { this.raison = raison; }
        
        @Override
        public String toString() {
            return String.format("%s (%s) - Score: %d%% - %s", nom, type, score, raison);
        }
    }
}
