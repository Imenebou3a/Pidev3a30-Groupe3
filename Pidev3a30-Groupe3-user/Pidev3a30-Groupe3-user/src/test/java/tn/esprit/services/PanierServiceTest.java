package tn.esprit.services;

import tn.esprit.entities.LignePanier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le service Panier
 */
class PanierServiceTest {
    
    private PanierService panierService;
    
    @BeforeEach
    void setUp() {
        panierService = PanierService.getInstance();
        panierService.viderPanier();
    }
    
    @Test
    void testAjouterProduit() {
        panierService.ajouterProduit(1, "Poterie", new BigDecimal("25.50"), "Artisanat • Nabeul", "");
        
        assertEquals(1, panierService.getLignes().size());
        assertEquals(1, panierService.getNombreArticles());
        assertEquals(new BigDecimal("25.50"), panierService.getTotal());
    }
    
    @Test
    void testAjouterKit() {
        panierService.ajouterKit(1, "Kit Poterie", new BigDecimal("45.00"), "Poterie • Facile", "");
        
        assertEquals(1, panierService.getLignes().size());
        assertEquals("KIT", panierService.getLignes().get(0).getTypeProduit());
    }
    
    @Test
    void testAjouterProduitExistant() {
        panierService.ajouterProduit(1, "Poterie", new BigDecimal("25.50"), "Artisanat • Nabeul", "");
        panierService.ajouterProduit(1, "Poterie", new BigDecimal("25.50"), "Artisanat • Nabeul", "");
        
        assertEquals(1, panierService.getLignes().size());
        assertEquals(2, panierService.getLignes().get(0).getQuantite());
        assertEquals(new BigDecimal("51.00"), panierService.getTotal());
    }
    
    @Test
    void testModifierQuantite() {
        panierService.ajouterProduit(1, "Poterie", new BigDecimal("25.50"), "Artisanat • Nabeul", "");
        LignePanier ligne = panierService.getLignes().get(0);
        
        panierService.modifierQuantite(ligne.getIdLigne(), 5);
        
        assertEquals(5, ligne.getQuantite());
        assertEquals(new BigDecimal("127.50"), panierService.getTotal());
    }
    
    @Test
    void testSupprimerLigne() {
        panierService.ajouterProduit(1, "Poterie", new BigDecimal("25.50"), "Artisanat • Nabeul", "");
        LignePanier ligne = panierService.getLignes().get(0);
        
        panierService.supprimerLigne(ligne.getIdLigne());
        
        assertTrue(panierService.getLignes().isEmpty());
        assertEquals(BigDecimal.ZERO, panierService.getTotal());
    }
    
    @Test
    void testViderPanier() {
        panierService.ajouterProduit(1, "Poterie", new BigDecimal("25.50"), "Artisanat • Nabeul", "");
        panierService.ajouterKit(1, "Kit Poterie", new BigDecimal("45.00"), "Poterie • Facile", "");
        
        panierService.viderPanier();
        
        assertTrue(panierService.getLignes().isEmpty());
        assertEquals(0, panierService.getNombreArticles());
    }
    
    @Test
    void testCalculTotal() {
        panierService.ajouterProduit(1, "Poterie", new BigDecimal("25.50"), "Artisanat • Nabeul", "");
        panierService.ajouterKit(1, "Kit Poterie", new BigDecimal("45.00"), "Poterie • Facile", "");
        panierService.ajouterProduit(2, "Tapis", new BigDecimal("120.00"), "Textile • Kairouan", "");
        
        assertEquals(new BigDecimal("190.50"), panierService.getTotal());
        assertEquals(3, panierService.getNombreArticles());
    }
    
    @Test
    void testSingleton() {
        PanierService instance1 = PanierService.getInstance();
        PanierService instance2 = PanierService.getInstance();
        
        assertSame(instance1, instance2);
    }
}
