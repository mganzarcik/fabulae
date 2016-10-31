package shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class WaterShader {
	static final public ShaderProgram createWaterShader() {
		final String vertexShader = 
							"attribute vec4 a_position;\n" +
							"attribute vec4 a_color;\n" +
							"attribute vec2 a_texCoord0;\n" +
							"uniform mat4 u_projTrans;\n" +
							"uniform vec2 waveData;\n" + //allows to make waaaves
							"varying vec4 vColor;\n" +
							"varying vec2 vTexCoord;\n" +
							"\n" +
							"void main() {\n" +
							"	vColor = a_color;\n" +
							"	vTexCoord = a_texCoord0;\n" +
							"	vec4 newPos = vec4(a_position.x + waveData.y * sin(waveData.x+a_position.x+a_position.y), a_position.y + waveData.y * sin(waveData.x+a_position.x+a_position.y), a_position.z, a_position.w);\n" +
							"	gl_Position = u_projTrans * newPos;\n" +	
							"}";
		final String pixelShader = 
							"#ifdef GL_ES\n" +
							"#define LOWP lowp\n" +
							"precision mediump float;\n" +
							"#else\n" +
							"#define LOWP\n" +
							"#endif\n" +
							
							"varying LOWP vec4 vColor;\n" +
							"varying vec2 vTexCoord;\n" +
							"\n" +
							//our texture samplers
							"uniform sampler2D u_texture;\n" + //diffuse map
							"\n" +
							"void main() {\n" +
							"	vec4 DiffuseColor = texture2D(u_texture, vTexCoord);\n" +
							"	gl_FragColor = vColor * DiffuseColor;\n" +
							"}";
		ShaderProgram.pedantic = false;
		ShaderProgram waterShader = new ShaderProgram(vertexShader,
				pixelShader);
		if (waterShader.isCompiled() == false) {
			Gdx.app.log("ERROR", waterShader.getLog());

		}

		return waterShader;
	}
}
