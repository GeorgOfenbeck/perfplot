
DIRNAME=PCM-MSR_Win
UTILNAME=pcm-msr

rm -rf $DIRNAME

mkdir $DIRNAME 
cp PCM-Power_Win/stdafx.h $DIRNAME/stdafx.h
cp PCM-Power_Win/stdafx.cpp $DIRNAME/stdafx.cpp

sed 's/pcm-power/'$UTILNAME'/g' PCM-Power_Win/pcm-power-win.cpp    > $DIRNAME/$UTILNAME-win.cpp

sed 's/pcm-power/'$UTILNAME'/g' PCM-Power_Win/pcm-power-win.vcproj > $DIRNAME/$UTILNAME-win.vcproj


