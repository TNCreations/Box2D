package box2d.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import static utils.Constant.PPM;
import utils.TiledObjectUtil;

public class Box2D extends ApplicationAdapter implements InputProcessor {

    SpriteBatch batch;
    Sprite sprVlad;
    ShapeRenderer SR;
    Texture txSheet, txBackground, txTemp, txOne, txWater, txInvIcon;
    int fW, fH, fSx, fSy;
    int nFrame, nPos;
    float fSpeed;
    float fAniSpeed;
    float fInvPosX, fInvPosY;
    private Box2DDebugRenderer b2dr;
    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer tmr, treeRender;
    private TiledMap map, trees;
    private World world;
    private Body player, platform;
    Animation araniVlad[];
    TextureRegion trTemp;

    @Override
    public void create() {
        float nWScreen, nHScreen;
        nWScreen = Gdx.graphics.getWidth();
        nHScreen = Gdx.graphics.getHeight();
        Gdx.input.setInputProcessor(this);
        SR = new ShapeRenderer();
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, nWScreen / 2, nHScreen / 2);

        world = new World(new Vector2(0, 0), false);
        b2dr = new Box2DDebugRenderer();

        map = new TmxMapLoader().load("gameMap.tmx");
        tmr = new OrthogonalTiledMapRenderer(map);
        trees = new TmxMapLoader().load("gameMapTrees.tmx");
        treeRender = new OrthogonalTiledMapRenderer(trees);
        
        TiledObjectUtil.parseTiledObjectLayer(world, map.getLayers().get("Object Layer 1").getObjects());

        player = createBox(500, 1200, 16, 32, false);
        platform = createBox(0, 0, 64, 32, true);

        nFrame = 0;
        nPos = 0; // the position in the SpriteSheet - 0 to 7
        txSheet = new Texture("playerSprite.png");
        txWater = new Texture("water.png");
        txInvIcon = new Texture("inventoryIcon.png");
        txWater.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        araniVlad = new Animation[18];
        playerSprite(5.2f);

    }

    @Override
    public void render() {
        update(Gdx.graphics.getDeltaTime());

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        frameAnimation();
        fInvPosX = player.getPosition().x / PPM;
        fInvPosY = player.getPosition().y / PPM;
        trTemp = araniVlad[nPos].getKeyFrame(nFrame, true);
        batch.begin();
        batch.draw(txWater,-129,-135,32,32,2500,2500); // makes water
        batch.end();
        tmr.render();
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        batch.draw(trTemp, player.getPosition().x * PPM - 16, player.getPosition().y * PPM - 16);
        batch.draw(txInvIcon,fInvPosX - 32, fInvPosY - 32);
        batch.end();
        treeRender.render();
//        SR.begin(ShapeType.Filled);
//        SR.setColor(Color.PINK);
//        SR.rect(fInvPosX,fInvPosY,32,32);
//        SR.end();
        b2dr.render(world, camera.combined.scl(PPM));
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width / 2, height / 2);
    }

    @Override
    public void dispose() {
        world.dispose();
        b2dr.dispose();
        batch.dispose();
        SR.dispose();
        tmr.dispose();
        treeRender.dispose();
        map.dispose();
        trees.dispose();
    }

    public void update(float delta) {
        world.step(1 / 60f, 6, 2);
        inputUpdate(delta);
        cameraUpdate(delta);

        tmr.setView(camera);
        treeRender.setView(camera);
        batch.setProjectionMatrix(camera.combined);
    }

    public void inputUpdate(float delta) {
        int nHorizontalForce = 0;
        int nVerticalForce = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            nVerticalForce += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            nVerticalForce -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            nHorizontalForce -= 1;
            nPos = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            nHorizontalForce += 1;
            nPos = 0;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            fSpeed = 2.5f;
            playerSprite(3.6f);
        } else {
            fSpeed = 1.5f;
            playerSprite(5.2f);
        }
        player.setLinearVelocity(nHorizontalForce * fSpeed, player.getLinearVelocity().y);
        player.setLinearVelocity(player.getLinearVelocity().x, nVerticalForce * fSpeed);
    }

    public void cameraUpdate(float delta) {
        Vector3 position = camera.position;
        position.x = player.getPosition().x * PPM;
        position.y = player.getPosition().y * PPM;
        camera.position.set(position);

        camera.update();
    }

    public Body createBox(int nX, int nY, int nWidth, int nHeight, boolean isStatic) {
        Body pBody;
        BodyDef def = new BodyDef();
        if (isStatic) {
            def.type = BodyDef.BodyType.StaticBody;
        } else {
            def.type = BodyDef.BodyType.DynamicBody;
        }
        def.position.set(nX / PPM, nY / PPM);
        def.fixedRotation = true;
        pBody = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(nWidth / 2 / PPM, nHeight / 2 / PPM);
        pBody.createFixture(shape, 1.0f);
        shape.dispose();
        return pBody;
    }

    public void playerSprite(float nAniSpeed) {
        fW = txSheet.getWidth() / 9;
        fH = txSheet.getHeight() / 2;
        for (int i = 0; i < 9; i++) {
            Sprite[] arSprVlad = new Sprite[9];
            for (int j = 0; j < 9; j++) {
                fSx = j * fW;
                fSy = i * fH;
                sprVlad = new Sprite(txSheet, fSx, fSy, fW, fH);
                arSprVlad[j] = new Sprite(sprVlad);
            }
            araniVlad[i] = new Animation(nAniSpeed, arSprVlad);

        }
    }

    public void frameAnimation() {
        if (!Gdx.input.isKeyPressed(Input.Keys.W) && !Gdx.input.isKeyPressed(Input.Keys.D)
                && !Gdx.input.isKeyPressed(Input.Keys.A) && !Gdx.input.isKeyPressed(Input.Keys.S)) {
            if (nPos == 0) {
                nFrame = 0;
            } else if (nPos == 1) {
                nFrame = 45; // I dont know why this works but it does
            }
        } else {
            nFrame++;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
       return false;
    }

    @Override
    public boolean keyTyped(char character) {
       return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        if (amount == 1 && camera.zoom <= 1.2) {
            camera.zoom += 0.1;
        } else if (amount == -1 && camera.zoom >= 0.4) {
            camera.zoom -= 0.1;
        }
        return false;
    }
    
    
    public void handleInput() {
        //https://github.com/libgdx/libgdx/wiki/Orthographic-camera        
        camera.zoom = MathUtils.clamp(camera.zoom, 1.5f, 1.8f);

        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, 
                Gdx.graphics.getWidth() - effectiveViewportWidth / 2f);
        camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f,
                Gdx.graphics.getHeight() - effectiveViewportHeight / 2f);
    }
}
