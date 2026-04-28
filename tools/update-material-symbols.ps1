$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$drawableDir = Join-Path $repoRoot "app/src/main/res/drawable"
$baseUrl = "https://raw.githubusercontent.com/google/material-design-icons/master/symbols/android"

$icons = [ordered]@{
    "ic_account_circle" = "account_circle"
    "ic_account_circle_thin" = "account_circle"
    "ic_add" = "add"
    "ic_arrow_back" = "arrow_back"
    "ic_arrow_drop_down" = "arrow_drop_down"
    "ic_arrow_forward" = "arrow_forward"
    "ic_article" = "article"
    "ic_build" = "build"
    "ic_check" = "check"
    "ic_chevron_left" = "chevron_left"
    "ic_chevron_right" = "chevron_right"
    "ic_close" = "close"
    "ic_delete_forever" = "delete_forever"
    "ic_done" = "done"
    "ic_edit" = "edit"
    "ic_error" = "error"
    "ic_event" = "event"
    "ic_exit_to_app" = "exit_to_app"
    "ic_group" = "group"
    "ic_info" = "info"
    "ic_language" = "language"
    "ic_local_library" = "local_library"
    "ic_output" = "output"
    "ic_place_item" = "place_item"
    "ic_play_circle" = "play_circle"
    "ic_policy" = "policy"
    "ic_publish" = "publish"
    "ic_remove" = "remove"
    "ic_schedule" = "schedule"
    "ic_school" = "school"
    "ic_settings_backup_restore" = "settings_backup_restore"
    "ic_settings" = "settings"
    "ic_share" = "share"
    "ic_tips_and_updates" = "tips_and_updates"
    "ic_today" = "today"
    "ic_volunteer_activism" = "volunteer_activism"
    "ic_warning" = "warning"
    "ic_work" = "work"
}

foreach ($entry in $icons.GetEnumerator()) {
    $resourceName = $entry.Key
    $symbolName = $entry.Value
    $url = "$baseUrl/$symbolName/materialsymbolsrounded/${symbolName}_24px.xml"
    $target = Join-Path $drawableDir "$resourceName.xml"

    Write-Host "Updating $resourceName from $symbolName"
    Invoke-WebRequest -Uri $url -OutFile $target
}
