import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;

public class GerenciadorDebug {

    // Método principal que pinta os quadrados coloridos.
    // Ele precisa receber o "jogo" inteiro para saber a posição de tudo.
    public void desenharHitboxes(Graphics2D g2d, JogoExploracao jogo, int cameraX, int cameraY) {
        Stroke tracoOriginal = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2)); // Deixa as linhas desenhadas mais grossas (visíveis)

        // --- 1. DESENHA O CHÃO (Linhas Verdes) ---
        // Pega a lista certa de chão dependendo de em qual mundo a Clara está
        List<Rectangle> zonasVerdes = (jogo.mundoUmbra && !jogo.portaUmbraDestrancada) ?
                jogo.getSistemaColisao().getZonasQuarto() : jogo.getSistemaColisao().getZonasCaminhaveis();

        g2d.setColor(Color.GREEN);
        for (Rectangle zona : zonasVerdes) {
            // Soma com a cameraX e cameraY para o quadrado se mover junto com o mapa na tela
            g2d.drawRect(zona.x + cameraX, zona.y + cameraY, zona.width, zona.height);
        }

        // --- 2. DESENHA OS MÓVEIS/OBSTÁCULOS (Linhas Vermelho Escuro) ---
        g2d.setColor(new Color(139, 0, 0));
        for (Rectangle obs : jogo.getSistemaColisao().getObstaculos()) {
            g2d.drawRect(obs.x + cameraX, obs.y + cameraY, obs.width, obs.height);
        }

        // --- 3. DESENHA OS ITENS DE INTERAÇÃO (Coloridos) ---
        if (!jogo.mundoUmbra) { // Itens que só existem no Mundo Real
            g2d.setColor(Color.CYAN); // Pílula
            g2d.drawRect(cameraX + jogo.areaPilula.x, cameraY + jogo.areaPilula.y, jogo.areaPilula.width, jogo.areaPilula.height);

            g2d.setColor(Color.PINK); // NPC da Recepção
            g2d.drawRect(cameraX + jogo.areaNPC.x, cameraY + jogo.areaNPC.y, jogo.areaNPC.width, jogo.areaNPC.height);

            g2d.setColor(Color.WHITE); // Documento de Texto na Recepção
            g2d.drawRect(cameraX + jogo.areaDocumento.x, cameraY + jogo.areaDocumento.y, jogo.areaDocumento.width, jogo.areaDocumento.height);
        } else { // Itens que só existem no Mundo Umbra (Pesadelo)
            g2d.setColor(Color.BLUE); // Cama de hospital
            g2d.drawRect(cameraX + jogo.areaCama.x, cameraY + jogo.areaCama.y, jogo.areaCama.width, jogo.areaCama.height);

            g2d.setColor(Color.ORANGE); // Porta de Saída do Quarto
            g2d.drawRect(cameraX + jogo.areaPortaUmbra.x, cameraY + jogo.areaPortaUmbra.y, jogo.areaPortaUmbra.width, jogo.areaPortaUmbra.height);

            g2d.setColor(Color.MAGENTA); // Espelho com o enigma do relógio
            g2d.drawRect(cameraX + jogo.areaEspelho.x, cameraY + jogo.areaEspelho.y, jogo.areaEspelho.width, jogo.areaEspelho.height);

            g2d.setColor(Color.YELLOW); // Gaveta com cadeado numérico
            g2d.drawRect(cameraX + jogo.areaGaveta.x, cameraY + jogo.areaGaveta.y, jogo.areaGaveta.width, jogo.areaGaveta.height);
        }

        // --- 4. DESENHA A HITBOX DA PERSONAGEM (Quadrado Vermelho) ---
        g2d.setColor(Color.RED);
        g2d.drawRect(jogo.mundoX + cameraX, jogo.mundoY + cameraY, jogo.Largura_Hitbox, jogo.Altura_Hitbox);

        // --- 5. TEXTO DE STATUS NA TELA ---
        g2d.setStroke(tracoOriginal); // Volta o pincel ao normal
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Modo Debug ON - Peças: " + jogo.partesObjetoCircular + "/3 | Missão: " + jogo.missaoAtual, 10, 20);
    }
}