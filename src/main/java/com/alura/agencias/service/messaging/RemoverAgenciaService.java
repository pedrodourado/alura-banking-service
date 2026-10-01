package com.alura.agencias.service.messaging;

import br.com.alura.Agencia;
import com.alura.agencias.domain.messaging.AgenciaMensagem;
import com.alura.agencias.service.AgenciaService;
import com.alura.agencias.service.http.saga.SagaHttpService;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class RemoverAgenciaService {

    private final AgenciaService agenciaService;
    @RestClient
    SagaHttpService sagaHttpService;
    public RemoverAgenciaService(AgenciaService agenciaService) {
        this.agenciaService = agenciaService;
    }

    @Incoming("remover-agencia-channel")
    @WithTransaction
    public Uni<Void> consumirMensagem(Agencia mensagem) {
        try {
            AgenciaMensagem agenciaMensagem =
                    new AgenciaMensagem(1,
                            mensagem.getNome(),
                            mensagem.getRazaoSocial(),
                            mensagem.getCnpj(),
                            mensagem.getSituacaoCadastral()
                    );

            return agenciaService.buscarPorCnpj(agenciaMensagem.getCnpj())
                    .onItem().ifNotNull().transformToUni(agencia ->
                        agenciaService.deletar(agencia.getId())
                                .call(() -> sagaHttpService.fecharSaga(agencia.getCnpj()))
                    ).replaceWithVoid();

        }catch (Exception e){
            return Uni.createFrom().failure(e);
        }
    }
}
