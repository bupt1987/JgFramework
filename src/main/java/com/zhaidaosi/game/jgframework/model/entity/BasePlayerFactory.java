package com.zhaidaosi.game.jgframework.model.entity;


public class BasePlayerFactory implements IBasePlayerFactory {

    public IBaseCharacter getPlayer() {
        return new BasePlayer();
    }

}
