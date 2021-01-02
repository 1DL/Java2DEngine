/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.luiz.engine.game.objects;

import com.luiz.engine.GameEngine;
import com.luiz.engine.Renderer;
import com.luiz.engine.game.GameManager;
import com.luiz.engine.game.components.AABBComponent;

/**
 *
 * @author Luiz
 */
public class Platform extends GameObject
{
    private int color = (int)(Math.random() * Integer.MAX_VALUE);
    public Platform(int x, int y)
    {
        this.tag = "platform";
        this.width = 32;
        this.height = 16;
        this.padding = 0;
        this.paddingTop = 0;
        this.posX = x;
        this.posY = y;
        
        this.addComponent(new AABBComponent(this));
    }
    
    @Override
    public void update(GameEngine ge, GameManager gm, float deltaTime)
    {
        this.updateComponents(ge, gm, deltaTime);
    }

    @Override
    public void render(GameEngine ge, Renderer r)
    {
        r.drawFillRect((int)posX, (int)posY, width, height, color);
        this.renderComponents(ge, r);
    }

    @Override
    public void collision(GameObject other)
    {
        color = (int) (Math.random() * Integer.MAX_VALUE);
    }
    
}
