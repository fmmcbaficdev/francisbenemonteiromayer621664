package br.gov.mt.seplag.backend.dto;

/**
 * DTO para resultado da sincronização de regionais
 */
public record SyncResultDTO(
        boolean sucesso,
        String mensagem,
        int criados,
        int atualizados,
        int desativados,
        int semMudancas,
        int totalApi,
        int totalBanco,
        long duracaoMs
) {
    /**
     * Total de registros processados
     */
    public int totalProcessados() {
        return criados + atualizados + desativados + semMudancas;
    }

    /**
     * Verifica se houve alguma alteração
     */
    public boolean houveAlteracao() {
        return criados > 0 || atualizados > 0 || desativados > 0;
    }

    /**
     * Factory para resultado de erro
     */
    public static SyncResultDTO erro(String mensagem) {
        return new SyncResultDTO(false, mensagem, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Factory para resultado de sucesso simples
     */
    public static SyncResultDTO sucesso(int criados, int atualizados, int desativados) {
        return new SyncResultDTO(
                true,
                "Sincronização concluída com sucesso",
                criados,
                atualizados,
                desativados,
                0, 0, 0, 0
        );
    }
}
