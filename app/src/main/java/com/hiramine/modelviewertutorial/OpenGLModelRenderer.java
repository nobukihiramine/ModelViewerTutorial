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

import javax.microedition.khronos.opengles.GL10;

public class OpenGLModelRenderer extends OpenGLTrackRenderer
{
	// メンバー変数
	private Model m_model;

	// アクセサ
	public Model getModel()
	{
		return m_model;
	}

	public void setModel( Model model )
	{
		m_model = model;
	}

	@Override
	protected void renderScene()
	{
		renderModel();
	}

	protected void renderModel()
	{
	}
}
