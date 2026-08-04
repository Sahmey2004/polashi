package com.sahmey.polashi.game;

import org.springframework.web.socket.WebSocketSession;
import java.util.UUID;


public final class Player{
    private final UUID id;
    private final String nickname;
    private Faction role;
    private Character character;
    private WebSocketSession session;


    public Player(String nickname, WebSocketSession session){
        this.id = UUID.randomUUID();
        this.nickname = nickname;
        this.session = session;
    }

    public UUID getId(){
        return id;
    }

    public String getNickname(){
        return nickname;
    }

    public Faction getRole(){
        return role;
    }

    public void setRole(Faction role){
        this.role = role;
    }

    public Character getCharacter(){
        return character;
    }

    public void setCharacter(Character character){
        this.character = character;
    }

    public WebSocketSession getSession(){
        return session;
    }
    public void setSession(WebSocketSession session){
        this.session = session;
    }
}