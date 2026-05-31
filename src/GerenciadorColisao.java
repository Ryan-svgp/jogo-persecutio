import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorColisao {
    // Listas que guardam os "quadrados" invisíveis do mapa
    private final List<Rectangle> zonasCaminhaveis; // Onde pode andar no Mundo Real
    private final List<Rectangle> zonasQuarto;      // Onde pode andar no Mundo Umbra (preso no quarto)
    private final List<Rectangle> obstaculos;       // Móveis e paredes onde o jogador bate e para

    public GerenciadorColisao() {
        // Inicializando as listas vazias
        this.zonasCaminhaveis = new ArrayList<>();
        this.zonasQuarto = new ArrayList<>();
        this.obstaculos = new ArrayList<>();

        // Chamando as funções que vão preencher essas listas
        configurarLimitesMapa();
        configurarObstaculos();
    }

    // Função que desenha o "chão" por onde o jogador pode andar
    private void configurarLimitesMapa() {
        int escala = 2; // O jogo é ampliado em 2x, então multiplicamos as colisões por 2 também

        // --- 1. ÁREAS DO QUARTO E BANHEIRO ---
        Rectangle quartoEsquerdo = new Rectangle(25 * escala, 275 * escala, 145 * escala, 205 * escala);
        Rectangle quartoBaixo = new Rectangle(150 * escala, 375 * escala, 100 * escala, 105 * escala);
        Rectangle banheiro = new Rectangle(170 * escala, 270 * escala, 85 * escala, 100 * escala);
        Rectangle portaBanheiro = new Rectangle(190 * escala, 340 * escala, 40 * escala, 55 * escala);

        // --- 2. ÁREAS DO CORREDOR E RECEPÇÃO ---
        Rectangle corredorHorizontal = new Rectangle(28 * escala, 495 * escala, 805 * escala, 90 * escala);
        Rectangle corredorVertical = new Rectangle(745 * escala, 260 * escala, 85 * escala, 270 * escala);
        Rectangle recepcao = new Rectangle(570 * escala, 48 * escala, 420 * escala, 200 * escala);

        // --- 3. ÁREAS DAS PORTAS DE CONEXÃO ---
        Rectangle portaQuarto = new Rectangle(195 * escala, 450 * escala, 35 * escala, 70 * escala);
        Rectangle portaRecepcao = new Rectangle(773 * escala, 220 * escala, 35 * escala, 70 * escala);

        // Adiciona TUDO na lista do Mundo Real (A Clara pode andar no mapa todo)
        zonasCaminhaveis.add(quartoEsquerdo);
        zonasCaminhaveis.add(quartoBaixo);
        zonasCaminhaveis.add(banheiro);
        zonasCaminhaveis.add(portaBanheiro);
        zonasCaminhaveis.add(corredorHorizontal);
        zonasCaminhaveis.add(corredorVertical);
        zonasCaminhaveis.add(recepcao);
        zonasCaminhaveis.add(portaQuarto);
        zonasCaminhaveis.add(portaRecepcao);

        // Adiciona SÓ O QUARTO na lista do Mundo Umbra (A Clara não consegue sair daqui até destrancar)
        zonasQuarto.add(quartoEsquerdo);
        zonasQuarto.add(quartoBaixo);
        zonasQuarto.add(banheiro);
        zonasQuarto.add(portaBanheiro);
    }

    // Função que cria os objetos sólidos (mesas, cadeiras, sofás)
    private void configurarObstaculos() {
        int escala = 2;

        // Exemplo: A Mesa da Enfermeira.
        // Se a hitbox da Clara encostar nisso, ela para de andar.
        Rectangle mesaEnfermeira = new Rectangle(600 * escala, 100 * escala, 120 * escala, 50 * escala);

        // Adiciona a mesa na lista de bloqueios
        obstaculos.add(mesaEnfermeira);
    }

    // O "Segurança" do jogo: Ele diz se o próximo passo da Clara é permitido ou não
    public boolean verificarPosicaoValida(int proximoX, int proximoY, int largura, int altura, boolean mundoUmbra, boolean portaUmbraDestrancada) {
        // Cria um quadrado imaginário para onde a Clara QUER ir
        Rectangle hitbox = new Rectangle(proximoX, proximoY, largura, altura);

        // 1º REGRA: Verifica se ela está batendo a cara em um móvel (obstáculo)
        for (Rectangle obs : obstaculos) {
            if (obs.intersects(hitbox)) {
                return false; // Se bater no móvel, bloqueia o movimento!
            }
        }

        // 2º REGRA: Define qual "chão" ela pode usar.
        // Se estiver no Umbra e a porta estiver trancada, usa a lista 'zonasQuarto'.
        // Caso contrário, usa a lista do mapa todo ('zonasCaminhaveis').
        List<Rectangle> zonasAtuais = (mundoUmbra && !portaUmbraDestrancada) ? zonasQuarto : zonasCaminhaveis;

        // Verifica se a Clara está 100% dentro do chão permitido
        for (Rectangle zona : zonasAtuais) {
            if (zona.contains(hitbox)) {
                return true; // Se estiver no chão verde, libera o passo!
            }
        }
        return false; // Se tentar sair do chão verde, bloqueia o movimento.
    }

    // Métodos para enviar essas listas para quem quiser ler (como o nosso GerenciadorDebug)
    public List<Rectangle> getZonasCaminhaveis() { return zonasCaminhaveis; }
    public List<Rectangle> getZonasQuarto() { return zonasQuarto; }
    public List<Rectangle> getObstaculos() { return obstaculos; }
}
