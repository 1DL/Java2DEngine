/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.luiz.engine.gfx;

/**
 *
 * @author Luiz
 */
public class ImageTile extends Image{
    
    private int tileW;
    private int tileH;
    
    public ImageTile(String path, int tileW, int tileH) {
        super(path);
        this.tileW = tileW;
        this.tileH = tileH;
    }
    
    public Image getTileImage(int tileX, int tileY)
    {
        int[] p = new int[tileW * tileH];
        
        for(int y = 0; y < tileH; y++)
        {
           for(int x = 0; x < tileH; x++)
            {
                p[x + y * tileW] = this.getP()[(x + tileX * tileW) + (y + tileY * tileH) * this.getW()];
            } 
        }
        
        return new Image(p, tileW, tileH);
    }

    public int getTileW() {
        return tileW;
    }

    public void setTileW(int tileW) {
        this.tileW = tileW;
    }

    public int getTileH() {
        return tileH;
    }

    public void setTileH(int tileH) {
        this.tileH = tileH;
    }
    
    
}
