#/bin/bash
# Renders SVG files to bitmaps
set -e

BIN=inkscape
UNIT_LENGTH=128 # Animation files need manual updating
ASSET_ROOT=../app/src/main/assets/Bitmaps
RES_ROOT=../app/src/main/res


# Render an image
# $1: Source SVG
# $2: Output png
# $3: input x offset (from left)
# $4: input y offset (from bottom)
# $5: input w
# $6: input h
# $7: output w
# $7: output h
function render_part_scaled () {
    x1=$(($3 + $5))
    y1=$(($4 + $6))
    ${BIN} $1 -a $3:$4:$x1:$y1 -w $7 -h $8 --export-png ${ASSET_ROOT}/$2
}

# Render an image
# $1: Source SVG
# $2: Output png
# $3: Output w
# $4: Output h
function render_whole_scaled () {
    ${BIN} $1 -w $3 -h $4 --export-png ${ASSET_ROOT}/$2
}

# Render an image
# $1: Source SVG
# $2: Output png
function render_whole () {
    ${BIN} $1 --export-png ${ASSET_ROOT}/$2
}


# Render an image at all the different DPIs
# $1: Source SVG
# $2: Output directory (without dpi)
# $3: Output filename
# $4: Output w for mdpi
# $5: Output h for mdpi
function render_whole_scaled_at_all_dpi () {
    mkdir -p ${RES_ROOT}/$2-ldpi/
    mkdir -p ${RES_ROOT}/$2-mdpi/
    mkdir -p ${RES_ROOT}/$2-hdpi/
    mkdir -p ${RES_ROOT}/$2-xhdpi/
    mkdir -p ${RES_ROOT}/$2-xxhdpi/
    mkdir -p ${RES_ROOT}/$2-xxxhdpi/
    ${BIN} $1 -w $(($4 * 3 / 4)) -h $(($5 * 3 / 4)) --export-png ${RES_ROOT}/$2-ldpi/$3
    ${BIN} $1 -w $(($4 * 1 / 1)) -h $(($5 * 1 / 1)) --export-png ${RES_ROOT}/$2-mdpi/$3
    ${BIN} $1 -w $(($4 * 3 / 2)) -h $(($5 * 3 / 2)) --export-png ${RES_ROOT}/$2-hdpi/$3
    ${BIN} $1 -w $(($4 * 2 / 1)) -h $(($5 * 2 / 1)) --export-png ${RES_ROOT}/$2-xhdpi/$3
    ${BIN} $1 -w $(($4 * 3 / 1)) -h $(($5 * 3 / 1)) --export-png ${RES_ROOT}/$2-xxhdpi/$3
    ${BIN} $1 -w $(($4 * 4 / 1)) -h $(($5 * 4 / 1)) --export-png ${RES_ROOT}/$2-xxxhdpi/$3
}

# Render an image at all the different DPIs
# $1: Source SVG
# $2: Output directory (without dpi)
# $3: Output filename
function render_whole_at_all_dpi () {
    mkdir -p ${RES_ROOT}/$2-ldpi/
    mkdir -p ${RES_ROOT}/$2-mdpi/
    mkdir -p ${RES_ROOT}/$2-hdpi/
    mkdir -p ${RES_ROOT}/$2-xhdpi/
    mkdir -p ${RES_ROOT}/$2-xxhdpi/
    mkdir -p ${RES_ROOT}/$2-xxxhdpi/

    w=`${BIN} --query-width $1`
    h=`${BIN} --query-height $1`

    ${BIN} $1 -w `echo "($w * 0.75 + 0.5) / 1" | bc` -h `echo "($h * 0.75 + 0.5) / 1" | bc` --export-png ${RES_ROOT}/$2-ldpi/$3
    ${BIN} $1 -w `echo "($w * 1.00 + 0.5) / 1" | bc` -h `echo "($h * 1.00 + 0.5) / 1" | bc` --export-png ${RES_ROOT}/$2-mdpi/$3
    ${BIN} $1 -w `echo "($w * 1.50 + 0.5) / 1" | bc` -h `echo "($h * 1.50 + 0.5) / 1" | bc` --export-png ${RES_ROOT}/$2-hdpi/$3
    ${BIN} $1 -w `echo "($w * 2.00 + 0.5) / 1" | bc` -h `echo "($h * 2.00 + 0.5) / 1" | bc` --export-png ${RES_ROOT}/$2-xhdpi/$3
    ${BIN} $1 -w `echo "($w * 3.00 + 0.5) / 1" | bc` -h `echo "($h * 3.00 + 0.5) / 1" | bc` --export-png ${RES_ROOT}/$2-xxhdpi/$3
    ${BIN} $1 -w `echo "($w * 4.00 + 0.5) / 1" | bc` -h `echo "($h * 4.00 + 0.5) / 1" | bc` --export-png ${RES_ROOT}/$2-xxxhdpi/$3
}

# Render an image at a particular dpi.
# Input image must be at correct resolution for mdpi
# $1: Source SVG
# $2: Output directory (without dpi)
# $3: DPI (ldpi/mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi)
# $4: Output filename
function render_whole_for_dpi () {
    w=`${BIN} --query-width $1`
    h=`${BIN} --query-height $1`
    
    case $3 in
    ldpi)
        w=`echo "($w * 0.75 + 0.5) / 1" | bc`
        h=`echo "($h * 0.75 + 0.5) / 1" | bc`
        ;;
    mdpi)
        w=`echo "($w * 1.0 + 0.5) / 1" | bc`
        h=`echo "($h * 1.0 + 0.5) / 1" | bc`
        ;;
    hdpi)
        w=`echo "($w * 1.5 + 0.5) / 1" | bc`
        h=`echo "($h * 1.5 + 0.5) / 1" | bc`
        ;;
    xhdpi)
        w=`echo "($w * 2.0 + 0.5) / 1" | bc`
        h=`echo "($h * 2.0 + 0.5) / 1" | bc`
        ;;
    xxhdpi)
        w=`echo "($w * 3.0 + 0.5) / 1" | bc`
        h=`echo "($h * 3.0 + 0.5) / 1" | bc`
        ;;
    xxxhdpi)
        w=`echo "($w * 4.0 + 0.5) / 1" | bc`
        h=`echo "($h * 4.0 + 0.5) / 1" | bc`
        ;;
    *)
        echo "Unsupported DPI '$3'"
        exit 1
        ;;
    esac;

    mkdir -p ${RES_ROOT}/$2-$3/
    ${BIN} $1 -w ${w} -h ${h} --export-png ${RES_ROOT}/$2-$3/$4
}

render_whole_scaled Arrows.svg                Game/Arrows-hdpi.png                 $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 1))
render_whole_scaled Arrows_MP.svg             Game/Arrows_MP.png                   $(($UNIT_LENGTH * 8))  $(($UNIT_LENGTH * 4))
render_whole_scaled Cursor.svg                Game/Cursor-hdpi.png                 $(($UNIT_LENGTH * 1))  $(($UNIT_LENGTH * 1))
render_whole_scaled Cursor-trans.svg          Game/Cursor-trans-hdpi.png           $(($UNIT_LENGTH * 1))  $(($UNIT_LENGTH * 1))
render_whole_scaled MPCursors.svg             Game/Cursors_MP.png                  $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 1))
render_whole_scaled GestureArrows.svg         Game/GestureArrows-hdpi.png          $(($UNIT_LENGTH * 8))  $(($UNIT_LENGTH * 2))
render_whole_scaled HalfArrows.svg            Game/HalfArrows-hdpi.png             $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 1))
render_whole_scaled Hole.svg                  Game/Hole-hdpi.png                   $(($UNIT_LENGTH * 1))  $(($UNIT_LENGTH * 1))
render_whole_scaled KapuKapu.svg              Game/KapuKapu-hdpi.png               $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 4 * 3 / 2))
render_whole_scaled KapuKapuDeath.svg         Game/KapuKapuDeath-hdpi.png          $(($UNIT_LENGTH * 8))  $(($UNIT_LENGTH * 3 / 2))
render_whole_scaled ShokoDeath.svg            Game/Mouse_ShokoWhiteDeath-hdpi.png  $(($UNIT_LENGTH * 2))  $(($UNIT_LENGTH * 1))
render_whole_scaled Shoko.svg                 Game/Mouse_ShokoWhite-hdpi.png       $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 4))
render_whole_scaled ShokoRescue.svg           Game/Mouse_ShokoWhiteRescue-hdpi.png $(($UNIT_LENGTH * 12)) $(($UNIT_LENGTH * 1))
render_whole_scaled Ring.svg                  Game/Ring-hdpi.png                   $(($UNIT_LENGTH * 1))  $(($UNIT_LENGTH * 1))
render_whole_scaled Rocket.svg                Game/Rocket-hdpi.png                 $(($UNIT_LENGTH * 1))  $(($UNIT_LENGTH * 1))
render_whole_scaled RocketMP.svg              Game/Rocket_MP.png                   $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 1))
render_whole_scaled Spawner.svg               Game/Spawner.png                     $(($UNIT_LENGTH * 1))  $(($UNIT_LENGTH * 1))
render_whole_scaled Tick.svg                  Game/Tick-hdpi.png                   $(($UNIT_LENGTH * 1))  $(($UNIT_LENGTH * 1))
render_whole_scaled TileA.svg                 Game/TileA-hdpi.png                  $(($UNIT_LENGTH * 1))  $(($UNIT_LENGTH * 1))
render_whole_scaled TileB.svg                 Game/TileB-hdpi.png                  $(($UNIT_LENGTH * 1))  $(($UNIT_LENGTH * 1))
render_whole_scaled Walls.svg                 Game/Walls-hdpi.png                  $(($UNIT_LENGTH * 1))  $(($UNIT_LENGTH * 76 / 64))

render_whole_scaled Christmas/Shoko.svg       Game/Christmas/Mouse.png             $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 4 * 3 / 2))
render_whole_scaled GoldMice/Shoko.svg        Game/Contributor/Mouse.png           $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 8))
render_whole_scaled GhostMice/Shoko.svg       Game/GhostMouse/Mouse.png            $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 8))
render_whole_scaled LineMice/Shoko.svg        Game/Line/Mouse.png                  $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 8))
render_whole_scaled LineMice/KapuKapu.svg     Game/Line/KapuKapu.png               $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 6 * 3 / 2))
render_whole_scaled BlackMice/Shoko.svg       Game/Obsidian/Mouse.png              $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 8))
render_whole_scaled PinkMice/Shoko.svg        Game/PinkMice/ShokoPink.png          $(($UNIT_LENGTH * 4))  $(($UNIT_LENGTH * 4))
render_whole_scaled PinkMice/ShokoDeath.svg   Game/PinkMice/ShokoPink.png          $(($UNIT_LENGTH * 2))  $(($UNIT_LENGTH * 1))
render_whole_scaled PinkMice/ShokoRescue.svg  Game/PinkMice/ShokoPinkRescue.png    $(($UNIT_LENGTH * 12)) $(($UNIT_LENGTH * 1))

render_whole Background.svg                   Game/Background.png     # Not sure this is used
render_whole BackgroundGradient.svg           Game/BackgroundGradient.png
render_whole CloudA.svg                       Game/CloudA.png
render_whole CloudB.svg                       Game/CloudB.png
render_whole CloudC.svg                       Game/CloudC.png
render_whole RocketLaunch.svg                 Game/RocketLaunch.png   # Not sure this is used

render_whole_scaled IntroLogo.svg             Intro/Logo.png         426  404
render_whole_scaled CreditRocket.svg          Intro/CreditRocket.png 128  270
render_whole_scaled Smoke.svg                 Intro/Smoke.png 1024 128

render_whole_scaled_at_all_dpi Blackboard.svg  drawable blackboard.png 240 180
render_whole_scaled_at_all_dpi Complete.svg    drawable complete.png   240 160
render_whole_scaled_at_all_dpi Icon.svg        drawable icon.png       48  48
render_whole_scaled_at_all_dpi Play.svg        drawable play.png       96  96

render_whole_at_all_dpi        BlankButton.svg     raw      blank_button.png
render_whole_at_all_dpi        BlankButton_MP0.svg raw      blank_mpbutton0.png
render_whole_at_all_dpi        BlankButton_MP1.svg raw      blank_mpbutton1.png
render_whole_at_all_dpi        BlankButton_MP2.svg raw      blank_mpbutton2.png
render_whole_at_all_dpi        BlankButton_MP3.svg raw      blank_mpbutton3.png
render_whole_at_all_dpi        RadioSet.svg        raw      blank_radio_set.png
render_whole_at_all_dpi        RadioUnset.svg      raw      blank_radio_unset.png


render_whole_for_dpi ArrowButtonRightLDPI.svg raw ldpi    arrow_button_down.png
render_whole_for_dpi ArrowButtonRightMDPI.svg raw mdpi    arrow_button_down.png 
render_whole_for_dpi ArrowButtonRightHDPI.svg raw hdpi    arrow_button_down.png 
render_whole_for_dpi ArrowButtonRightHDPI.svg raw hdpi    arrow_button_down.png 
render_whole_for_dpi ArrowButtonRightHDPI.svg raw xhdpi   arrow_button_down.png
render_whole_for_dpi ArrowButtonRightHDPI.svg raw xxhdpi  arrow_button_down.png
render_whole_for_dpi ArrowButtonRightHDPI.svg raw xxxhdpi arrow_button_down.png

render_whole_for_dpi ArrowButtonLeftLDPI.svg raw ldpi    arrow_button_left.png
render_whole_for_dpi ArrowButtonLeftMDPI.svg raw mdpi    arrow_button_left.png 
render_whole_for_dpi ArrowButtonLeftHDPI.svg raw hdpi    arrow_button_left.png 
render_whole_for_dpi ArrowButtonLeftHDPI.svg raw hdpi    arrow_button_left.png 
render_whole_for_dpi ArrowButtonLeftHDPI.svg raw xhdpi   arrow_button_left.png
render_whole_for_dpi ArrowButtonLeftHDPI.svg raw xxhdpi  arrow_button_left.png
render_whole_for_dpi ArrowButtonLeftHDPI.svg raw xxxhdpi arrow_button_left.png

render_whole_for_dpi ArrowButtonUpLDPI.svg raw ldpi    arrow_button_up.png
render_whole_for_dpi ArrowButtonUpMDPI.svg raw mdpi    arrow_button_up.png 
render_whole_for_dpi ArrowButtonUpHDPI.svg raw hdpi    arrow_button_up.png 
render_whole_for_dpi ArrowButtonUpHDPI.svg raw hdpi    arrow_button_up.png 
render_whole_for_dpi ArrowButtonUpHDPI.svg raw xhdpi   arrow_button_up.png
render_whole_for_dpi ArrowButtonUpHDPI.svg raw xxhdpi  arrow_button_up.png
render_whole_for_dpi ArrowButtonUpHDPI.svg raw xxxhdpi arrow_button_up.png

render_whole_for_dpi ArrowButtonRightLDPI.svg raw ldpi    arrow_button_right.png
render_whole_for_dpi ArrowButtonRightMDPI.svg raw mdpi    arrow_button_right.png 
render_whole_for_dpi ArrowButtonRightHDPI.svg raw hdpi    arrow_button_right.png 
render_whole_for_dpi ArrowButtonRightHDPI.svg raw hdpi    arrow_button_right.png 
render_whole_for_dpi ArrowButtonRightHDPI.svg raw xhdpi   arrow_button_right.png
render_whole_for_dpi ArrowButtonRightHDPI.svg raw xxhdpi  arrow_button_right.png
render_whole_for_dpi ArrowButtonRightHDPI.svg raw xxxhdpi arrow_button_right.png

