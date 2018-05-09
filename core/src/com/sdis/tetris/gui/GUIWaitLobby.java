package com.sdis.tetris.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.sdis.tetris.Buttons;
import com.sdis.tetris.Tetris;
import com.sdis.tetris.audio.SFX;
import com.sdis.tetris.audio.Song;

public class GUIWaitLobby  extends GUIScreen{
    private final Stage stage = new Stage();
    private final Table table = new Table();
    Sprite background = new Sprite(new Texture(Gdx.files.internal("img/main_menu.png"), false));
    Sprite title = new Sprite(new Texture(Gdx.files.internal("img/main_title.png"), false));
    private final TextButton backButton = new TextButton("< BACK", Buttons.MenuButton);
    private final List<String> list;
    private Skin skin;
    ScrollPane scrollPane;
    private float gameWidth = Gdx.graphics.getWidth();
    private float gameHeight = Gdx.graphics.getHeight();

    private class Back implements Runnable
    {
        @Override
        public void run()
        {
            parent.switchTo(new GUIMultiPlayer(parent));
        }
    }

    public GUIWaitLobby(Tetris paramParent) {
        super(paramParent, Song.THEME_A);
        background.setPosition(0,0);
        background.setSize((float)Gdx.graphics.getWidth(),(float)Gdx.graphics.getHeight());
        title.setPosition((float)Gdx.graphics.getWidth()/3.7f,(float)Gdx.graphics.getHeight()-title.getHeight()*2);
        skin  = new Skin(Gdx.files.internal("menu/myskin.json"), new TextureAtlas(Gdx.files.internal("menu/atlas.atlas")));
        list=new List<String>(skin);
        String[] strings = new String[4];
        for (int i = 0, k = 0; i < 4; i++) {
            strings[k++] = "String: " + i;
        }
        list.setItems(strings);
        list.setAlignment(1);
        scrollPane = new ScrollPane(list);
        scrollPane.setBounds(0, 0, 5, 20);
        scrollPane.setSmoothScrolling(false);
        scrollPane.setPosition(gameWidth / 2 - scrollPane.getWidth() / 4,
                gameHeight / 2 - scrollPane.getHeight() / 4);
        scrollPane.setTransform(true);
        scrollPane.setScale(0.5f);
        table.add(list).size((float)Gdx.graphics.getWidth()/2, (float)Gdx.graphics.getHeight()/8).padBottom(10).row();
        table.add(scrollPane);
        table.row();
        table.add(backButton).size((float)Gdx.graphics.getWidth()/2, (float)Gdx.graphics.getHeight()/8).padBottom(10).row();
        table.setFillParent(true);
        table.setVisible(true);
        stage.addActor(table);
        backButton.setPosition(48, 30);
        backButton.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                audio.playSFX(SFX.HOVER);
                stage.addAction(Actions.sequence(Actions.moveTo(-480.0f, 0.0f, 0.5f), Actions.run(new Back())));
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
            {
                if (! backButton.isPressed())
                {
                    audio.playSFX(SFX.SELECT);
                }
            }
        });
    }

    @Override
    public void render(float delta)
    {
        Gdx.gl.glClearColor( 1, 1, 1, 1 );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

        stage.act();

        batch.begin();

        background.draw(batch);
        title.draw(batch);

        batch.end();
        stage.draw();
    }

    @Override
    public void resize(int width, int height)
    {
        stage.getViewport().update(width, height);
        //camera.position.set(camera.viewportWidth/2, camera.viewportHeight/2, 0);
    }

    @Override
    public void show()
    {
        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void dispose()
    {
        stage.dispose();
        skin.dispose();
    }
}
