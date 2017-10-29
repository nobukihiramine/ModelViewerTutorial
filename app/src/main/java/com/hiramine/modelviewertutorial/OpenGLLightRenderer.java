package com.hiramine.modelviewertutorial;

import android.opengl.GLU;

import javax.microedition.khronos.opengles.GL10;

public class OpenGLLightRenderer extends OpenGLPickRenderer
{
	// メンバー変数
	private LightSetting[] m_aLightSetting;

	// コンストラクタ
	public OpenGLLightRenderer()
	{
		m_aLightSetting = new LightSetting[4];
		m_aLightSetting[0] = new LightSetting();
		m_aLightSetting[1] = new LightSetting();
		m_aLightSetting[2] = new LightSetting();
		m_aLightSetting[3] = new LightSetting();
		m_aLightSetting[0].m_bLightEnabled = true;
		float[] f4LightDiffuse  = { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] f4LightSpecular = { 0.2f, 0.2f, 0.2f, 1.0f };
		m_aLightSetting[0].m_fbLightDiffuse = OpenGLBaseRenderer.makeFloatBuffer( f4LightDiffuse );
		m_aLightSetting[0].m_fbLightSpecular = OpenGLBaseRenderer.makeFloatBuffer( f4LightSpecular );
	}

	private void SetupLight()
	{
		GL10 gl = getGL();
		for( int nIndexLight = 0; nIndexLight < 4; nIndexLight++ )
		{
			if( m_aLightSetting[nIndexLight].m_bLightEnabled )
			{
				gl.glEnable( GL10.GL_LIGHT0 + nIndexLight );
				gl.glLightfv( GL10.GL_LIGHT0 + nIndexLight, GL10.GL_POSITION, m_aLightSetting[nIndexLight].m_fbLightPosition );
				gl.glLightfv( GL10.GL_LIGHT0 + nIndexLight, GL10.GL_AMBIENT, m_aLightSetting[nIndexLight].m_fbLightAmbient );
				gl.glLightfv( GL10.GL_LIGHT0 + nIndexLight, GL10.GL_DIFFUSE, m_aLightSetting[nIndexLight].m_fbLightDiffuse );
				gl.glLightfv( GL10.GL_LIGHT0 + nIndexLight, GL10.GL_SPECULAR, m_aLightSetting[nIndexLight].m_fbLightSpecular );
				gl.glLightfv( GL10.GL_LIGHT0 + nIndexLight, GL10.GL_SPOT_DIRECTION, m_aLightSetting[nIndexLight].m_fbLightSpotDirection );
				gl.glLightf( GL10.GL_LIGHT0 + nIndexLight, GL10.GL_SPOT_EXPONENT, m_aLightSetting[nIndexLight].m_fLightSpotExponent );
				gl.glLightf( GL10.GL_LIGHT0 + nIndexLight, GL10.GL_SPOT_CUTOFF, m_aLightSetting[nIndexLight].m_fLightSpotCutoff );
				gl.glLightf( GL10.GL_LIGHT0 + nIndexLight, GL10.GL_CONSTANT_ATTENUATION, m_aLightSetting[nIndexLight].m_fLightConstantAttenuation );
				gl.glLightf( GL10.GL_LIGHT0 + nIndexLight, GL10.GL_LINEAR_ATTENUATION, m_aLightSetting[nIndexLight].m_fLightLinearAttenuation );
				gl.glLightf( GL10.GL_LIGHT0 + nIndexLight, GL10.GL_QUADRATIC_ATTENUATION, m_aLightSetting[nIndexLight].m_fLightQuadraticAttenuation );
			}
			else
			{
				gl.glDisable( GL10.GL_LIGHT0 + nIndexLight );
			}
		}
	}

	@Override
	protected void setupViewingTransform()
	{
		GL10 gl = getGL();
		gl.glMatrixMode( GL10.GL_MODELVIEW );
		gl.glLoadIdentity();
		GLU.gluLookAt( gl,
					   m_fRenderingCenterX, m_fRenderingCenterY, 500,
					   m_fRenderingCenterX, m_fRenderingCenterY, 0.0f,
					   0.0f, 1.0f, 0.0f );

		SetupLight();

		gl.glMultMatrixf( m_f16ObjectForm, 0 ); // 表示回転（＝モデル回転）

		setViewingTransformValid( true );
	}
}
