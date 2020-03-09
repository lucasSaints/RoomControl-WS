package br.com.wises.services;

import br.com.wises.database.EManager;
import br.com.wises.database.pojo.Reserva;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.json.JSONObject;

@Path("reserva")
public class ReservaService {

    @GET
    @Path("byIdSala")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Reserva> getReservasByIdSala(
            @HeaderParam("id_sala") int idSala,
            @HeaderParam("authorization") String authorization) {
        if (authorization != null && authorization.equals("secret")) {
            List<Reserva> lista = EManager.getInstance().getDbAccessor().getReservasByIdSala(idSala);
            return lista;
        } else {
            return null;
        }
    }

    @GET
    @Path("byIdUsuario")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Reserva> getReservasByIdUsuario(
            @HeaderParam("id_usuario") int idUsuario,
            @HeaderParam("authorization") String authorization) {
        if (authorization != null && authorization.equals("secret")) {
            List<Reserva> lista = EManager.getInstance().getDbAccessor().getReservasByIdUsuario(idUsuario);
            return lista;
        } else {
            return null;
        }
    }
    
    @POST
    @Path("cancelar")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String cancelarReserva(@HeaderParam("authorization") String authorization, @HeaderParam("id") int id){
        if (authorization.equals("secret")) {
            String res=EManager.getInstance().getDbAccessor().cancelReserva(id);
            return "200. Reserva cancelada com sucesso\n\n<><><><><<><><><><>\n\n"+res+"\n\n<><><><><><><>\n\n";
        } else {
            return "403. Token inválido";
        }
    }

    @POST
    @Path("cadastrar")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String createReserva(
            @HeaderParam("novaReserva") String novaReservaEncoded,
            @HeaderParam("authorization") String authorization) {
        if (authorization != null && authorization.equals("secret")) {
            String reservaDecoded = new String(Base64.getDecoder().decode(novaReservaEncoded.getBytes()), Charset.forName("UTF-8"));
            JSONObject userObj = new JSONObject(reservaDecoded);
            Reserva novaReserva = new Reserva();
            System.out.println("Reserva Encoded: " + novaReservaEncoded);
            System.out.println("Reserva Decoded: " + reservaDecoded);
            int idSala = 0, idUsuario = 0;
            String descricao = "", repeticoes = "";
            Date dataHoraInicio = null, dataHoraFim = null;
            if (userObj.has("id_sala") && userObj.has("id_usuario") && userObj.has("descricao") && userObj.has("data_hora_fim") && userObj.has("ativo")) {
                idSala = userObj.getInt("id_sala");
                boolean deu=true;
                if(userObj.has("data_hora_inicio"))
                    dataHoraInicio = new Date(userObj.getLong("data_hora_inicio"));
                else
                    repeticoes = new String(userObj.getString("repeticoes"));
                dataHoraFim = new Date(userObj.getLong("data_hora_fim"));
                for(int i=0;i<EManager.getInstance().getDbAccessor().getReservasByIdSala(idSala).size();i++){
                    if(EManager.getInstance().getDbAccessor().getReservasByIdSala(idSala).get(i).getDataHoraInicio().compareTo(dataHoraInicio)<0 && EManager.getInstance().getDbAccessor().getReservasByIdSala(idSala).get(i).getDataHoraFim().compareTo(dataHoraInicio)>0)
                        deu=false;
                    if(EManager.getInstance().getDbAccessor().getReservasByIdSala(idSala).get(i).getDataHoraInicio().compareTo(dataHoraFim)<0 && EManager.getInstance().getDbAccessor().getReservasByIdSala(idSala).get(i).getDataHoraFim().compareTo(dataHoraFim)>0)
                        deu=false;
                    System.out.println("deu: "+deu);
                }
                if(deu){
                    idUsuario = userObj.getInt("id_usuario");
                    descricao = userObj.getString("descricao");

                    novaReserva.setIdSala(idSala);
                    novaReserva.setIdUsuario(idUsuario);
                    novaReserva.setDescricao(descricao);
                    novaReserva.setDataHoraInicio(dataHoraInicio);
                    novaReserva.setDataHoraFim(dataHoraFim);
                    novaReserva.setAtivo(true);

                    Date date = new Date();
                    Calendar c = Calendar.getInstance();
                    c.setTime(date);
                    c.add(c.HOUR, -3);
                    date = c.getTime();

                    novaReserva.setDataCriacao(date);
                    novaReserva.setDataAlteracao(date);

                    EManager.getInstance().getDbAccessor().novaReserva(novaReserva);
                    return "Reserva realizada com sucesso";
                }else
                    return "A sala já foi reservada neste horário";
            } else {
                return "A reserva não foi realizada";
            }
        } else {
            return "A reserva não foi realizada. Token inválido.";
        }
    }

}
