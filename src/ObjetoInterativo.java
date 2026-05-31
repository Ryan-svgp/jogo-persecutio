import java.awt.*;

public class ObjetoInterativo {
    public String nome;
    public int mundoX;
    public int mundoY;
    public int largura;
    public int altura;
    public boolean ativoNoMundoReal;
    public boolean ativoNoMundoUmbra;

    // Construtor
    public ObjetoInterativo(String nome, int x, int y, int largura, int altura, boolean noReal, boolean noUmbra) {
        this.nome = nome;
        this.mundoX = x;
        this.mundoY = y;
        this.largura = largura;
        this.altura = altura;
        this.ativoNoMundoReal = noReal;
        this.ativoNoMundoUmbra = noUmbra;
    }

    // Retorna a área de colisão do objeto para sabermos se o jogador está perto
    public Rectangle getArea() {
        return new Rectangle(mundoX, mundoY, largura, altura);
    }
}