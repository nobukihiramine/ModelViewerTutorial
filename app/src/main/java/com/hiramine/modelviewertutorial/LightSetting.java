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

public class LightSetting
{
	// メンバー変数
	public boolean     m_bLightEnabled;
	public FloatBuffer m_fbLightPosition;
	public FloatBuffer m_fbLightAmbient;
	public FloatBuffer m_fbLightDiffuse;
	public FloatBuffer m_fbLightSpecular;
	public FloatBuffer m_fbLightSpotDirection;
	public float       m_fLightSpotExponent;            // 値の許容範囲[0, 128]
	public float       m_fLightSpotCutoff;            // 値の許容範囲[0, 90] or 180
	public float       m_fLightConstantAttenuation;
	public float       m_fLightLinearAttenuation;
	public float       m_fLightQuadraticAttenuation;

	// コンストラクタ
	public LightSetting()
	{
		m_bLightEnabled = false;
		float[] f4LightPosition        = { 0.0f, 0.0f, 1.0f, 0.0f };
		float[] m_f4LightAmbient       = { 0.0f, 0.0f, 0.0f, 1.0f };
		float[] m_f4LightDiffuse       = { 0.0f, 0.0f, 0.0f, 1.0f };
		float[] m_f4LightSpecular      = { 0.0f, 0.0f, 0.0f, 1.0f };
		float[] m_f3LightSpotDirection = { 0.0f, 0.0f, -1.0f };
		m_fbLightPosition = OpenGLBaseRenderer.makeFloatBuffer( f4LightPosition );
		m_fbLightAmbient = OpenGLBaseRenderer.makeFloatBuffer( m_f4LightAmbient );
		m_fbLightDiffuse = OpenGLBaseRenderer.makeFloatBuffer( m_f4LightDiffuse );
		m_fbLightSpecular = OpenGLBaseRenderer.makeFloatBuffer( m_f4LightSpecular );
		m_fbLightSpotDirection = OpenGLBaseRenderer.makeFloatBuffer( m_f3LightSpotDirection );
		m_fLightSpotExponent = 0; // 値の許容範囲[0, 128]
		m_fLightSpotCutoff = 180.0f; // 値の許容範囲[0, 90] or 180
		m_fLightConstantAttenuation = 1.0f;
		m_fLightLinearAttenuation = 0.0f;
		m_fLightQuadraticAttenuation = 0.0f;
	}
}
