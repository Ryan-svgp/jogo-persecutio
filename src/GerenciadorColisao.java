import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorColisao {
    // Lista privada de áreas possíveis de se mover
    private final List<Rectangle> zonasCaminhaveis;

    public GerenciadorColisao() {
        this.zonasCaminhaveis = new ArrayList<>();
        configurarLimitesMapa(); // AJUSTADO: Nome unificado com o método abaixo
    }

    /*
    * Define os retângulos correspondentes ao chão que pode se mover
    * O multiplicador 'escala' garante que acompanhe o tamanho da tela
    */
private void configurarLimitesMapa() { // AJUSTADO: Corrigida a digitação do nome do método
        int escala = 2;

        // ==========================================
        // 1. QUARTO FATIADO (Substituído o bloco único para reter o personagem)
        // ==========================================
        // Parte Esquerda: Vai da parede até a quina esquerda do banheiro (largura reduzida para 145)
        int quartoEsquerdoX = 25;
        int quartoEsquerdoY = 275;
        int quartoEsquerdoLargura = 145; 
        int quartoEsquerdoAltura = 205;
        Rectangle quartoEsquerdo = new Rectangle(quartoEsquerdoX * escala, quartoEsquerdoY * escala, quartoEsquerdoLargura * escala, quartoEsquerdoAltura * escala);

        // Parte Baixo: Fica logo abaixo do chão do banheiro (começa em X=170 e desce em Y=375)
        int quartoBaixoX = 150;
        int quartoBaixoY = 375;
        int quartoBaixoLargura = 100; 
        int quartoBaixoAltura = 105;
        Rectangle quartoBaixo = new Rectangle(quartoBaixoX * escala, quartoBaixoY * escala, quartoBaixoLargura * escala, quartoBaixoAltura * escala);
        // ==========================================

        // 2. BANHEIRO 
        int banheiroX = 170;
        int banheiroY = 270;
        int banheiroLargura = 85;
        int banheiroAltura = 100; 
        Rectangle banheiro = new Rectangle(banheiroX * escala, banheiroY * escala, banheiroLargura * escala, banheiroAltura * escala);

        // 3. PASSAGEM DA PORTA DO BANHEIRO (Ajustado número do comentário)
        int portaX = 195;
        int portaY = 340; 
        int portaLargura = 35; 
        int portaAltura = 55;  
        Rectangle portaBanheiro = new Rectangle(portaX * escala, portaY * escala, portaLargura * escala, portaAltura * escala);

        // 4. CORREDOR HORIZONTAL (Ajustado número do comentário)
        int corrHorizX = 28;
        int corrHorizY = 495;
        int corrHorizLargura = 805;
        int corrHorizAltura = 90;
        Rectangle corredorHorizontal = new Rectangle(corrHorizX * escala, corrHorizY * escala, corrHorizLargura * escala, corrHorizAltura * escala);

        // 5. CORREDOR VERTICAL (Ajustado número do comentário)
        int corrVertX = 745;
        int corrVertY = 260;
        int corrVertLargura = 85;
        int corrVertAltura = 270;
        Rectangle corredorVertical = new Rectangle(corrVertX * escala, corrVertY * escala, corrVertLargura * escala, corrVertAltura * escala);

        // 6. RECEPÇÃO (Ajustado número do comentário)
        int recepcaoX = 570;
        int recepcaoY = 48;
        int recepcaoLargura = 420;
        int recepcaoAltura = 200;
        Rectangle recepcao = new Rectangle(recepcaoX * escala, recepcaoY * escala, recepcaoLargura * escala, recepcaoAltura * escala);

        // ==========================================
        // NOVAS PORTAS ADICIONADAS
        // ==========================================

        // 1.1 PORTA DO QUARTO (Conecta a parte de baixo do quarto ao corredor horizontal)
        int portaQuartoX = 195;       
        int portaQuartoY = 450;       
        int portaQuartoLargura = 35;  
        int portaQuartoAltura = 70;   
        Rectangle portaQuarto = new Rectangle(portaQuartoX * escala, portaQuartoY * escala, portaQuartoLargura * escala, portaQuartoAltura * escala);

        // 5.1 PORTA DA RECEPÇÃO (Conecta a recepção ao topo do corredor vertical)
        int portaRecepcaoX = 773;      
        int portaRecepcaoY = 220;      
        int portaRecepcaoLargura = 35; 
        int portaRecepcaoAltura = 70;  
        Rectangle portaRecepcao = new Rectangle(portaRecepcaoX * escala, portaRecepcaoY * escala, portaRecepcaoLargura * escala, portaRecepcaoAltura * escala);

        // ==========================================

        // Adiciona todos os elementos na lista (Substituído o quarto antigo pelas duas partes novas)
        zonasCaminhaveis.add(quartoEsquerdo);
        zonasCaminhaveis.add(quartoBaixo);
        zonasCaminhaveis.add(banheiro);
        zonasCaminhaveis.add(portaBanheiro); 
        zonasCaminhaveis.add(corredorHorizontal);
        zonasCaminhaveis.add(corredorVertical);
        zonasCaminhaveis.add(recepcao);
        
        // Adicionando as novas pontes de colisão
        zonasCaminhaveis.add(portaQuarto);
        zonasCaminhaveis.add(portaRecepcao);
    }

    /**
     * Valida se a próxima posição que o personagem quer ir está totalmente contida
     * dentro de alguma das formas geométricas permitidas.
     */
    public boolean verificarPosicaoValida(int proximoX, int proximoY, int largura, int altura) {
        Rectangle hitbox = new Rectangle(proximoX, proximoY, largura, altura);

        // Se a hitbox estiver contida dentro de qualquer uma das zonas, o movimento é válido
        for (Rectangle zona : zonasCaminhaveis) {
            if (zona.contains(hitbox)) {
                return true;
            }
        }
        return false; // Retorna false se o jogador tentar sair do formato do mapa
    }

    public List<Rectangle> getZonasCaminhaveis() {
        return zonasCaminhaveis;
    }
}