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

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity
{
	// メンバー変数
	private GLSurfaceView m_glsurfaceview;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		// GLSurfaceViewの取得
		m_glsurfaceview = (GLSurfaceView)findViewById( R.id.glview );

		// Rendererの作成と、GLSurfaceViewへのセット
		m_glsurfaceview.setRenderer( new OpenGLBaseRenderer() );

		// 絶え間ないレンダリングではなく都度のレンダリング（setRenderer()よりも後に呼び出す必要あり）
		m_glsurfaceview.setRenderMode( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
	}

	// 初回表示時、および、ポーズからの復帰時
	@Override
	protected void onResume()
	{
		super.onResume();

		m_glsurfaceview.onResume();
	}

	// 別のアクティビティ（か別のアプリ）に移行したことで、バックグラウンドに追いやられた時
	@Override
	protected void onPause()
	{
		m_glsurfaceview.onPause();

		super.onPause();
	}
}
