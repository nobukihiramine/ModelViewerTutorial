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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class ModelViewerView extends GLSurfaceView
{
	// メンバー変数
	private OpenGLTrackRenderer m_renderer;

	// コンストラクタ
	public ModelViewerView( Context context, AttributeSet attrs )
	{
		super( context, attrs );

		// Rendererの作成
		m_renderer = new OpenGLTrackRenderer();

		// GLSurfaceViewにRendererをセット
		setRenderer( m_renderer );

		// 絶え間ないレンダリングではなく都度のレンダリング（setRenderer()よりも後に呼び出す必要あり）
		setRenderMode( GLSurfaceView.RENDERMODE_WHEN_DIRTY );
	}

	@Override
	public boolean onTouchEvent( MotionEvent event )
	{
		int         action     = event.getAction();
		int         pointcount = event.getPointerCount();
		final float fX         = event.getX();
		final float fY         = event.getY();

		switch( action & MotionEvent.ACTION_MASK )
		{
			// トラッキングの開始
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				if( 1 == pointcount )
				{ // １つの指でドラッグ
					// １本指ドラッグで行うトラッキングモードを指定する
					m_renderer.beginTracking( fX, fY, OpenGLTrackRenderer.ETrackingMode.TM_ROTATE );
				}
				else if( 2 == pointcount )
				{ // ２つの指でドラッグ
					// ２つの指が遠いときは、ズーム、近い時は、パン
					float       fX1       = event.getX( 1 );
					float       fY1       = event.getY( 1 );
					final float fDistance = (float)Math.sqrt( ( fX1 - fX ) * ( fX1 - fX ) + ( fY1 - fY ) * ( fY1 - fY ) );
					if( 300 < fDistance )
					{ // ズーム
						m_renderer.beginTracking( -fDistance, -fDistance, OpenGLTrackRenderer.ETrackingMode.TM_ZOOM );
					}
					else
					{ // パン
						m_renderer.beginTracking( fX, fY, OpenGLTrackRenderer.ETrackingMode.TM_PAN );
					}
				}
				break;
			// トラッキングの終了
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				m_renderer.endTracking();
				break;
			// トラッキング
			case MotionEvent.ACTION_MOVE:
				if( 2 == pointcount
					&& OpenGLTrackRenderer.ETrackingMode.TM_ZOOM == m_renderer.getTrackingMode() )
				{ // ズームは、１つ目の指の座標値ではなく、２つの指の距離で処理する。
					float       fX1       = event.getX( 1 );
					float       fY1       = event.getY( 1 );
					final float fDistance = (float)Math.sqrt( ( fX1 - fX ) * ( fX1 - fX ) + ( fY1 - fY ) * ( fY1 - fY ) );
					m_renderer.doTracking( -fDistance, -fDistance );
					requestRender(); // 再描画
				}
				else
				{ // 通常は、１つ目の指の座標値で処理する。
					m_renderer.doTracking( fX, fY );
					requestRender(); // 再描画
				}
				break;
		}

		return true;
	}
}
