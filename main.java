import java.util.ArrayList;
import java.util.List;

public abstract class Cliente {
    private String nome;
    private String email;

    public Cliente(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public abstract String getIdentificadorDocumento();
}

public class PessoaFisica extends Cliente {
    private String cpf;

    public PessoaFisica(String nome, String email, String cpf) {
        super(nome, email);
        this.cpf = cpf;
    }

    @Override
    public String getIdentificadorDocumento() {
        return cpf;
    }
}

public class PessoaJuridica extends Cliente {
    private String cnpj;

    public PessoaJuridica(String nome, String email, String cnpj) {
        super(nome, email);
        this.cnpj = cnpj;
    }

    @Override
    public String getIdentificadorDocumento() {
        return cnpj;
    }
}

public class CarteiraInvestimentos {
    private Cliente cliente;
    private List<Investimento> investimentos;

    public CarteiraInvestimentos(Cliente cliente) {
        this.cliente = cliente;
        this.investimentos = new ArrayList<>();
    }

    public void adicionarInvestimento(Investimento investimento) {
        if (investimento.getCliente().equals(this.cliente)) {
            investimentos.add(investimento);
        } else {
            throw new IllegalArgumentException("O investimento não pertence ao cliente desta carteira");
        }
    }

    public double calcularValorTotalInvestido() {
        double total = 0;
        for (Investimento investimento : investimentos) {
            total += investimento.getSaldo();
        }
        return total;
    }

    public List<Investimento> getInvestimentos() {
        return investimentos;
    }

    public Cliente getCliente() {
        return cliente;
    }
}

public abstract class Investimento {
    private Cliente cliente;
    private double saldo;

    public Investimento(Cliente cliente, double valorInicial) {
        this.cliente = cliente;
        this.saldo = valorInicial;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public double getSaldo() {
        return saldo;
    }

    protected void setSaldo(double saldo) {
        this.saldo = Math.max(saldo, 0);
    }

    public void aplicar(double valor) {
        if (valor > 0) {
            this.saldo += valor;
        }
    }

    public void resgatar(double valor) {
        if (valor > 0 && valor <= this.saldo) {
            this.saldo -= valor;
        }
    }

    public abstract double calcularSaldoProjetado(int numeroMeses);
    public abstract void simularPassagemDeMes();
}

public class TesouroPrefixado extends Investimento {
    private String nomeTitulo;
    private double taxaJurosAnual;
    private double percentualImpostoRenda;

    public TesouroPrefixado(PessoaFisica cliente, double valorInicial, String nomeTitulo, 
                          double taxaJurosAnual) {
        super(cliente, valorInicial);
        if (!(cliente instanceof PessoaFisica)) {
            throw new IllegalArgumentException("Tesouro Prefixado é exclusivo para Pessoa Física");
        }
        this.nomeTitulo = nomeTitulo;
        this.taxaJurosAnual = taxaJurosAnual;
        this.percentualImpostoRenda = 0.15; 
    }

    @Override
    public double calcularSaldoProjetado(int numeroMeses) {
        double taxaJurosMensal = taxaJurosAnual / 12;
        double saldoBrutoProjetado = getSaldo() * Math.pow(1 + taxaJurosMensal, numeroMeses);
        double rendimentoBruto = saldoBrutoProjetado - getSaldo();
        double impostoDevido = rendimentoBruto * percentualImpostoRenda;
        return getSaldo() + rendimentoBruto - impostoDevido;
    }

    @Override
    public void simularPassagemDeMes() {
        double rendimento = getSaldo() * (taxaJurosAnual / 12.0);
        setSaldo(getSaldo() + rendimento);
    }
}

public class AcaoBolsa extends Investimento {
    private String codigoAcao;
    private String nomeEmpresa;
    private double taxaCorretagemFixaMensal;

    public AcaoBolsa(Cliente cliente, double valorInicial, String codigoAcao, 
                    String nomeEmpresa, double taxaCorretagemFixaMensal) {
        super(cliente, valorInicial);
        this.codigoAcao = codigoAcao;
        this.nomeEmpresa = nomeEmpresa;
        this.taxaCorretagemFixaMensal = taxaCorretagemFixaMensal;
    }

    @Override
    public double calcularSaldoProjetado(int numeroMeses) {
        double saldoProjetado = getSaldo() * Math.pow(1.008, numeroMeses);
        double totalTaxas = taxaCorretagemFixaMensal * numeroMeses;
        return Math.max(saldoProjetado - totalTaxas, 0);
    }

    @Override
    public void simularPassagemDeMes() {
        setSaldo(getSaldo() * 1.008);
        setSaldo(getSaldo() - taxaCorretagemFixaMensal);
    }
}

public class FundoInvestimento extends Investimento {
    private String nomeFundo;
    private String cnpjGestora;
    private double taxaAdministracaoAnual;

    public FundoInvestimento(Cliente cliente, double valorInicial, String nomeFundo, 
                           String cnpjGestora, double taxaAdministracaoAnual) {
        super(cliente, valorInicial);
        this.nomeFundo = nomeFundo;
        this.cnpjGestora = cnpjGestora;
        this.taxaAdministracaoAnual = taxaAdministracaoAnual;
    }

    @Override
    public double calcularSaldoProjetado(int numeroMeses) {
        double saldoProjetado = getSaldo();
        double taxaAdmMensal = taxaAdministracaoAnual / 12;
        
        for (int i = 0; i < numeroMeses; i++) {
            double rendimento = saldoProjetado * 0.01;
            double taxaAdm = saldoProjetado * taxaAdmMensal;
            saldoProjetado += rendimento - taxaAdm;
        }
        
        return Math.max(saldoProjetado, 0);
    }

    @Override
    public void simularPassagemDeMes() {
        double rendimentoBruto = getSaldo() * 0.01;
        double taxaAdmDevida = getSaldo() * (taxaAdministracaoAnual / 12.0);
        setSaldo(getSaldo() + rendimentoBruto - taxaAdmDevida);
    }
}

public class Debenture extends Investimento {
    private String nomeEmpresaEmissora;
    private double taxaJurosAnualDebenture;
    private double percentualTributacaoPJ;

    public Debenture(PessoaJuridica cliente, double valorInicial, String nomeEmpresaEmissora, 
                    double taxaJurosAnualDebenture, double percentualTributacaoPJ) {
        super(cliente, valorInicial);
        if (!(cliente instanceof PessoaJuridica)) {
            throw new IllegalArgumentException("Debenture é exclusivo para Pessoa Jurídica");
        }
        this.nomeEmpresaEmissora = nomeEmpresaEmissora;
        this.taxaJurosAnualDebenture = taxaJurosAnualDebenture;
        this.percentualTributacaoPJ = percentualTributacaoPJ;
    }

    @Override
    public double calcularSaldoProjetado(int numeroMeses) {
        double taxaJurosMensal = taxaJurosAnualDebenture / 12;
        double saldoBrutoProjetado = getSaldo() * Math.pow(1 + taxaJurosMensal, numeroMeses);
        double rendimentoBruto = saldoBrutoProjetado - getSaldo();
        double impostoDevido = rendimentoBruto * percentualTributacaoPJ;
        return getSaldo() + rendimentoBruto - impostoDevido;
    }

    @Override
    public void simularPassagemDeMes() {
        double rendimento = getSaldo() * (taxaJurosAnualDebenture / 12.0);
        setSaldo(getSaldo() + rendimento);
    }
}

public class main {
    public static void main(String[] args) {
        PessoaFisica pf1 = new PessoaFisica("João Silva", "joao@email.com", "123.456.789-00");
        PessoaJuridica pj1 = new PessoaJuridica("Empresa XYZ", "contato@xyz.com", "12.345.678/0001-99");
        
        CarteiraInvestimentos carteiraPF1 = new CarteiraInvestimentos(pf1);
        CarteiraInvestimentos carteiraPJ1 = new CarteiraInvestimentos(pj1);
        
        TesouroPrefixado tesouro = new TesouroPrefixado(pf1, 5000, "Tesouro Prefixado 2029", 0.10);
        AcaoBolsa acao1 = new AcaoBolsa(pf1, 3000, "PETR4", "Petrobras PN", 10);
        FundoInvestimento fundo1 = new FundoInvestimento(pf1, 8000, "Fundo Alfa", "00.000.000/0001-00", 0.02);
        
        Debenture debenture1 = new Debenture(pj1, 15000, "Empresa ABC", 0.12, 0.20);
        AcaoBolsa acao2 = new AcaoBolsa(pj1, 7000, "VALE3", "Vale ON", 15);
        FundoInvestimento fundo2 = new FundoInvestimento(pj1, 10000, "Fundo Beta", "11.111.111/0001-11", 0.015);
        
        carteiraPF1.adicionarInvestimento(tesouro);
        carteiraPF1.adicionarInvestimento(acao1);
        carteiraPF1.adicionarInvestimento(fundo1);
        
        carteiraPJ1.adicionarInvestimento(debenture1);
        carteiraPJ1.adicionarInvestimento(acao2);
        carteiraPJ1.adicionarInvestimento(fundo2);
        
        System.out.println("=== Situação Inicial ===");
        exibirCarteira(carteiraPF1);
        exibirCarteira(carteiraPJ1);
        
        tesouro.aplicar(1000);
        acao1.aplicar(500);
        debenture1.aplicar(3000);
        
        System.out.println("\n=== Após Aplicações ===");
        exibirCarteira(carteiraPF1);
        exibirCarteira(carteiraPJ1);
        
        fundo1.resgatar(1000);
        acao2.resgatar(1500);
        
        System.out.println("\n=== Após Resgates ===");
        exibirCarteira(carteiraPF1);
        exibirCarteira(carteiraPJ1);
        
        for (int mes = 1; mes <= 3; mes++) {
            simularMes(carteiraPF1);
            simularMes(carteiraPJ1);
            
            System.out.println("\n=== Após " + mes + " mês(es) ===");
            exibirCarteira(carteiraPF1);
            exibirCarteira(carteiraPJ1);
        }
        
        System.out.println("\n=== Projeções para 12 meses ===");
        exibirProjecoes(carteiraPF1, 12);
        exibirProjecoes(carteiraPJ1, 12);
        
        try {
            TesouroPrefixado tesouroInvalido = new TesouroPrefixado(pj1, 5000, "Tesouro Inválido", 0.10);
        } catch (IllegalArgumentException e) {
            System.out.println("\nErro esperado ao criar TesouroPrefixado para PJ: " + e.getMessage());
        }
        
        try {
            Debenture debentureInvalida = new Debenture(pf1, 5000, "Empresa Inválida", 0.12, 0.20);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro esperado ao criar Debenture para PF: " + e.getMessage());
        }
    }
    
    private static void exibirCarteira(CarteiraInvestimentos carteira) {
        System.out.println("\nCarteira de " + carteira.getCliente().getNome() + ":");
        System.out.printf("Valor total investido: R$ %.2f\n", carteira.calcularValorTotalInvestido());
        
        for (Investimento inv : carteira.getInvestimentos()) {
            System.out.printf("- %s: R$ %.2f\n", 
                inv.getClass().getSimpleName(), 
                inv.getSaldo());
        }
    }
    
    private static void exibirProjecoes(CarteiraInvestimentos carteira, int meses) {
        System.out.println("\nProjeções para " + carteira.getCliente().getNome() + " em " + meses + " meses:");
        
        for (Investimento inv : carteira.getInvestimentos()) {
            System.out.printf("- %s: R$ %.2f (projetado) → R$ %.2f\n", 
                inv.getClass().getSimpleName(), 
                inv.getSaldo(),
                inv.calcularSaldoProjetado(meses));
        }
    }
    
    private static void simularMes(CarteiraInvestimentos carteira) {
        for (Investimento inv : carteira.getInvestimentos()) {
            inv.simularPassagemDeMes();
        }
    }
}
