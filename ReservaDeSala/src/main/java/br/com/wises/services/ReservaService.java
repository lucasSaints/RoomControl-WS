package br.com.wises.services;

import br.com.wises.database.EManager;
import br.com.wises.database.pojo.Reserva;
import br.com.wises.database.pojo.Sala;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    
    @GET
    @Path("byEmpresa")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Reserva> getReservasAtivasByEmpresa(@HeaderParam("id_org") int idEmp, @HeaderParam("authorization") String authorization){
        if (authorization != null && authorization.equals("secret")) {
            List<Sala> salasNaEmpresa = EManager.getInstance().getDbAccessor().getSalasByOrganizacaoId(idEmp);
            List<Reserva> reservasNaEmpresa = new ArrayList<>();
            for(int i=0;i<salasNaEmpresa.size();i++){
                List<Reserva> reservasNaSala = EManager.getInstance().getDbAccessor().getReservasByIdSala(salasNaEmpresa.get(i).getId());
                for(int j=0;j<reservasNaSala.size();j++){
                    if(reservasNaSala.get(j).getAtivo())
                        reservasNaEmpresa.add(reservasNaSala.get(j));
                }
            }
            return reservasNaEmpresa; //
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
            if (userObj.has("id_sala") && userObj.has("id_usuario") && userObj.has("descricao") && userObj.has("data_hora_inicio") && userObj.has("data_hora_fim") && userObj.has("ativo")) {
                idSala = userObj.getInt("id_sala");
                boolean deu=true;
                dataHoraInicio = new Date(userObj.getLong("data_hora_inicio"));
                if(userObj.has("repeticoes"))
                    repeticoes = new String(userObj.getString("repeticoes"));
                dataHoraFim = new Date(userObj.getLong("data_hora_fim"));
                for(int i=0;i<EManager.getInstance().getDbAccessor().getReservasByIdSala(idSala).size();i++){
                    Reserva res = EManager.getInstance().getDbAccessor().getReservasByIdSala(idSala).get(i);
                    if(res.getRepeticoes().isEmpty()){
                        if(res.getDataHoraInicio().compareTo(dataHoraInicio)<0 && res.getDataHoraFim().compareTo(dataHoraInicio)>0)
                            deu=false;
                        if(res.getDataHoraInicio().compareTo(dataHoraFim)<0 && res.getDataHoraFim().compareTo(dataHoraFim)>0)
                            deu=false;
                    }else{  
                        Calendar j = Calendar.getInstance();
                        j.setTime(dataHoraInicio);
                        while(j.getTime().compareTo(res.getDataHoraFim())<0){
                            if(res.getRepeticoes().charAt((j.get(j.DAY_OF_WEEK)*2)-2)=='1'){
                                Date tentativaInicio = new Date(2000,1,1,j.get(Calendar.HOUR_OF_DAY),j.get(Calendar.MINUTE));
                                Date tentativaFim = new Date(2000,1,1,dataHoraFim.getHours(),dataHoraFim.getMinutes());
                                Date repeticaoInicio = new Date(2000,1,1,res.getDataHoraInicio().getHours(),res.getDataHoraInicio().getMinutes());
                                Date repeticaoFim = new Date(2000,1,1,res.getDataHoraFim().getHours(),res.getDataHoraFim().getMinutes());
                                if(repeticaoInicio.compareTo(tentativaInicio)<0 && repeticaoFim.compareTo(tentativaInicio)>0)
                                    deu=false;
                                if(repeticaoInicio.compareTo(tentativaFim)<0 && repeticaoFim.compareTo(tentativaFim)>0)
                                    deu=false;
                            }
                            j.add(j.DAY_OF_MONTH, 1);
                        }
                    }
                    System.out.println("deu: "+deu);
                }
                if(deu){
                    idUsuario = userObj.getInt("id_usuario");
                    descricao = userObj.getString("descricao");
                    String repetecos="0,0,0,0,0,0,0";
                    novaReserva.setIdSala(idSala);
                    novaReserva.setIdUsuario(idUsuario);
                    novaReserva.setDescricao(descricao);
                    novaReserva.setDataHoraInicio(dataHoraInicio);
                    novaReserva.setDataHoraFim(dataHoraFim);
                    if(userObj.has("repeticoes"))
                        repetecos=userObj.getString("repeticoes");
                    novaReserva.setRepeticoes(repetecos);
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
