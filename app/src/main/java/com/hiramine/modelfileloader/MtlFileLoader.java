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
package com.hiramine.modelfileloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

import android.util.Log;

public class MtlFileLoader
{
	// 定数
	private static final String NONAME = "(NoName)";

	public static MFLMaterial[] load( String strPath )
	{
		File file = new File( strPath );
		if( 0 == file.length() )
		{
			return null;
		}

		// ファーストパース（要素数カウント）
		int[] aiCountMaterial = new int[1];
		if( !parse_first( strPath, aiCountMaterial ) )
		{
			return null;
		}

		// 領域確保
		if( 0 == aiCountMaterial[0] )
		{
			return null;
		}
		MFLMaterial[] aMaterial = new MFLMaterial[aiCountMaterial[0]];
		for( int i = 0; i < aMaterial.length; ++i )
		{
			aMaterial[i] = new MFLMaterial();
		}
		aMaterial[0].strName = NONAME;

		// セカンドパース（値詰め）
		if( !parse_second( strPath, aMaterial ) )
		{
			return null;
		}

		return aMaterial;
	}

	private static boolean parse_first( String strPath, int[] aiCountMaterial )
	{
		// インプットのチェック
		if( null == aiCountMaterial )
		{
			return false;
		}

		// アウトプットの初期化
		aiCountMaterial[0] = 0;

		try
		{
			// 読み取り
			BufferedReader br = new BufferedReader( new FileReader( strPath ) );

			int iCountMaterial = 1; // (NoName)分
			while( true )
			{
				String strReadString = br.readLine();
				if( null == strReadString )
				{
					break;
				}
				StringTokenizer stReadString = new StringTokenizer( strReadString, ", \t\r\n" );
				if( !stReadString.hasMoreTokens() )
				{
					continue;
				}
				String token = stReadString.nextToken();
				switch( token.charAt( 0 ) )
				{
					case 'n': // material
						++iCountMaterial;
						break;
				}
			}

			br.close();

			aiCountMaterial[0] = iCountMaterial;
			return true;
		}
		catch( Exception e )
		{
			Log.e( "MtlFileLoader", "parse_first error : " + e );
			return false;
		}
	}

	private static boolean parse_second( String strPath, MFLMaterial[] aMaterial )
	{
		// インプットのチェック
		if( null == aMaterial )
		{
			return false;
		}

		try
		{
			// 読み取り
			BufferedReader br = new BufferedReader( new FileReader( strPath ) );

			float       f0;
			float       f1;
			float       f2;
			int         iIndexMaterial   = 0;
			MFLMaterial material_current = aMaterial[iIndexMaterial];
			while( true )
			{
				String strReadString = br.readLine();
				if( null == strReadString )
				{
					break;
				}
				StringTokenizer stReadString = new StringTokenizer( strReadString, ", \t\r\n" );
				if( !stReadString.hasMoreTokens() )
				{
					continue;
				}
				String token = stReadString.nextToken();
				switch( token.charAt( 0 ) )
				{
					case '#': // コメント
						break;
					case 'n': // マテリアル
						++iIndexMaterial;
						material_current = aMaterial[iIndexMaterial];
						if( stReadString.hasMoreTokens() )
						{
							material_current.strName = stReadString.nextToken();
						}
						else
						{
							material_current.strName = NONAME;
						}
						break;
					case 'N':
						if( stReadString.hasMoreTokens() )
						{
							material_current.fShininess = Float.parseFloat( stReadString.nextToken() );
						}
						// wavefrontフォーマットのshininessは[0, 1000]、OpenGLのshininessは[0, 128]
						material_current.fShininess /= 1000.0;
						material_current.fShininess *= 128.0;
						break;
					case 'K':
						f0 = 0.0f;
						f1 = 0.0f;
						f2 = 0.0f;
						if( stReadString.hasMoreTokens() )
						{
							f0 = Float.parseFloat( stReadString.nextToken() );
						}
						if( stReadString.hasMoreTokens() )
						{
							f1 = Float.parseFloat( stReadString.nextToken() );
						}
						if( stReadString.hasMoreTokens() )
						{
							f2 = Float.parseFloat( stReadString.nextToken() );
						}
						switch( token.charAt( 1 ) )
						{
							case 'a':
								material_current.f4Ambient[0] = f0;
								material_current.f4Ambient[1] = f1;
								material_current.f4Ambient[2] = f2;
								break;
							case 'd':
								material_current.f4Diffuse[0] = f0;
								material_current.f4Diffuse[1] = f1;
								material_current.f4Diffuse[2] = f2;
								break;
							case 's':
								material_current.f4Specular[0] = f0;
								material_current.f4Specular[1] = f1;
								material_current.f4Specular[2] = f2;
								break;
							default:
								break;
						}
						break;
					default:
						break;
				}
			}

			br.close();

			return true;
		}
		catch( Exception e )
		{
			Log.e( "MtlFileLoader", "parse_second" );
			return false;
		}
	}
}
