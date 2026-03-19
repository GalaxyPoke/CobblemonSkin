Get-ChildItem ../common/src/main/resources/assets/cobblemon/bedrock/animations/ -Recurse | Where{$_.Name -Match ".bbmodel"} | Remove-Item
Get-ChildItem ../common/src/main/resources/assets/cobblemon/bedrock/animations/ -Recurse | Where{$_.Name -Match ".geo.json"} | Remove-Item
Get-ChildItem ../common/src/main/resources/assets/cobblemon/bedrock/animations/ -Recurse | Where{$_.Name -Match ".png"} | Remove-Item
Get-ChildItem ../common/src/main/resources/assets/cobblemon/bedrock/models/ -Recurse | Where{$_.Name -Match ".bbmodel"} | Remove-Item
Get-ChildItem ../common/src/main/resources/assets/cobblemon/bedrock/models/ -Recurse | Where{$_.Name -Match ".animation.json"} | Remove-Item
Get-ChildItem ../common/src/main/resources/assets/cobblemon/bedrock/models/ -Recurse | Where{$_.Name -Match ".png"} | Remove-Item
Get-ChildItem ../common/src/main/resources/assets/cobblemon/textures/pokemon/ -Recurse | Where{$_.Name -Match ".bbmodel"} | Remove-Item
Get-ChildItem ../common/src/main/resources/assets/cobblemon/textures/pokemon/ -Recurse | Where{$_.Name -Match ".animation.json"} | Remove-Item
Get-ChildItem ../common/src/main/resources/assets/cobblemon/textures/pokemon/ -Recurse | Where{$_.Name -Match ".geo.json"} | Remove-Item


