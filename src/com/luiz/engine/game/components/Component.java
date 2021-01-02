/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.luiz.engine.game.components;

import com.luiz.engine.GameEngine;
import com.luiz.engine.Renderer;
import com.luiz.engine.game.GameManager;

/**
 *
 * @author Luiz
 */
public abstract class Component
{
    protected String tag;
    
    public abstract void update(GameEngine ge, GameManager gm, float deltaTime);
    public abstract void render(GameEngine ge, Renderer r);

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }
    
    
}
