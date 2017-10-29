/*
 * Copyright 2017 Nobuki HIRAMINE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hiramine.modelviewertutorial;

import java.nio.FloatBuffer;

public class Material
{
	// メンバー変数
	private FloatBuffer m_fbAmbient;
	private FloatBuffer m_fbDiffuse;
	private FloatBuffer m_fbSpecular;
	private float       m_fShininess;

	// コンストラクタ
	public Material( float[] f4Ambient, float[] f4Diffuse, float[] f4Specular, float fShininess )
	{
		m_fbAmbient = OpenGLBaseRenderer.makeFloatBuffer( f4Ambient );
		m_fbDiffuse = OpenGLBaseRenderer.makeFloatBuffer( f4Diffuse );
		m_fbSpecular = OpenGLBaseRenderer.makeFloatBuffer( f4Specular );
		m_fShininess = fShininess;
	}

	public Material()
	{
		float[] f4Ambient  = { 0.2f, 0.2f, 0.2f, 1.0f };
		float[] f4Diffuse  = { 0.8f, 0.8f, 0.8f, 1.0f };
		float[] f4Specular = { 0.0f, 0.0f, 0.0f, 1.0f };
		float   fShininess = 0.0f;
		m_fbAmbient = OpenGLBaseRenderer.makeFloatBuffer( f4Ambient );
		m_fbDiffuse = OpenGLBaseRenderer.makeFloatBuffer( f4Diffuse );
		m_fbSpecular = OpenGLBaseRenderer.makeFloatBuffer( f4Specular );
		m_fShininess = fShininess;
	}

	// アクセサ
	public FloatBuffer getAmbientBuffer()
	{
		return m_fbAmbient;
	}

	public FloatBuffer getDiffuseBuffer()
	{
		return m_fbDiffuse;
	}

	public FloatBuffer getSpecularBuffer()
	{
		return m_fbSpecular;
	}

	public float getShininess()
	{
		return m_fShininess;
	}
}
