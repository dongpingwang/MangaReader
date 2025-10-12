#define _POSIX_C_SOURCE 200809L
#include <limits.h>
#ifndef MAXNAMLEN
#define MAXNAMLEN NAME_MAX
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <libgen.h>
#include "EPUB3.h"
#include "EPUB3_private.h"

#include <android/log.h>
#define TAG "EPUB3"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#include <unistd.h>
#include <sys/types.h>

// 实现一个使用 fd 的 fopen 函数
static voidpf ZCALLBACK fd_fopen(voidpf opaque, const void* filename, int mode) {
    // 这里我们忽略 filename 参数，因为我们直接使用 fd
    // opaque 将被设置为我们的文件描述符 (fd)
    return opaque;
}

// 实现 fread
static uLong ZCALLBACK fd_fread(voidpf opaque, voidpf stream, void* buf, uLong size) {
    int fd = (int)(intptr_t)opaque;
    ssize_t readSize = read(fd, buf, size);
    return (readSize >= 0) ? (uLong)readSize : 0;
}

// 实现 fseek
static ZPOS64_T ZCALLBACK fd_fseek(voidpf opaque, voidpf stream, ZPOS64_T offset, int origin) {
    int fd = (int)(intptr_t)opaque;
    off64_t result = lseek64(fd, offset, origin);
    return (result == -1) ? -1 : 0;
}

// 实现 ftell
static ZPOS64_T ZCALLBACK fd_ftell(voidpf opaque, voidpf stream) {
    int fd = (int)(intptr_t)opaque;
    off64_t result = lseek64(fd, 0, SEEK_CUR);
    return (result == -1) ? -1 : result;
}

// 实现 fclose
static int ZCALLBACK fd_fclose(voidpf opaque, voidpf stream) {
    int fd = (int)(intptr_t)opaque;
    return close(fd);
}

// 获取当前文件大小的实现 (可选，但推荐)
static int ZCALLBACK fd_error(voidpf opaque, voidpf stream) {
    // 简单返回 0 表示无错误，可根据需要实现
    return 0;
}

const char * kEPUB3TypeID = "_EPUB3_t";
const char * kEPUB3MetadataTypeID = "_EPUB3Metadata_t";
const char * kEPUB3MetadataItemTypeID = "_EPUB3MetadataItem_t";
const char * kEPUB3ManifestTypeID = "_EPUB3Manifest_t";
const char * kEPUB3ManifestItemTypeID = "_EPUB3ManifestItem_t";
const char * kEPUB3SpineTypeID = "_EPUB3Spine_t";
const char * kEPUB3SpineItemTypeID = "_EPUB3SpineItem_t";
const char * kEPUB3TocTypeID = "_EPUB3Toc_t";
const char * kEPUB3TocItemTypeID = "_EPUB3TocItem_t";


#ifndef PARSE_CONTEXT_STACK_DEPTH
#define PARSE_CONTEXT_STACK_DEPTH 64
#endif

#ifndef PARSE_CONTEXT_NCX_STACK_DEPTH
#define PARSE_CONTEXT_NCX_STACK_DEPTH 1024
#endif

#pragma mark - Public Query API

EXPORT int32_t EPUB3CountOfSequentialResources(EPUB3Ref epub)
{
  assert(epub != NULL);
  assert(epub->spine != NULL);
  return epub->spine->linearItemCount;
}

EXPORT EPUB3Error EPUB3GetPathsOfSequentialResources(EPUB3Ref epub, const char ** resources)
{
  assert(epub != NULL);
  assert(epub->spine != NULL);

  EPUB3Error error = kEPUB3Success;

  if(epub->spine->linearItemCount > 0) {
    int32_t count = 0;
    EPUB3SpineItemListItemPtr itemPtr = epub->spine->head;
    while(itemPtr != NULL) {
      if(itemPtr->item->isLinear) {
        resources[count] = itemPtr->item->manifestItem->href;
        count++;
      }
      itemPtr = itemPtr->next;
    }

  }

  return error;
}

EXPORT int32_t EPUB3CountOfTocRootItems(EPUB3Ref epub)
{
  assert(epub != NULL);
  assert(epub->toc != NULL);

  return epub->toc->rootItemCount;
}

EXPORT EPUB3Error EPUB3GetTocRootItems(EPUB3Ref epub, EPUB3TocItemRef *tocItems)
{
  assert(epub != NULL);
  assert(epub->toc != NULL);

  EPUB3Error error = kEPUB3Success;

  if(epub->toc->rootItemCount > 0) {
    int32_t count = 0;
    EPUB3TocItemChildListItemPtr itemPtr = epub->toc->rootItemsHead;
    while(itemPtr != NULL) {
      tocItems[count] = itemPtr->item;
      count++;
      itemPtr = itemPtr->next;
    }

  }

  return error;
}

EXPORT EPUB3Bool EPUB3TocItemHasParent(EPUB3TocItemRef tocItem)
{
  assert(tocItem != NULL);

  return tocItem->parent == NULL ? kEPUB3_NO : kEPUB3_YES;
}

EXPORT EPUB3TocItemRef EPUB3TocItemGetParent(EPUB3TocItemRef tocItem)
{
  assert(tocItem != NULL);

  return tocItem->parent;
}

EXPORT int32_t EPUB3TocItemCountOfChildren(EPUB3TocItemRef tocItem)
{
  assert(tocItem != NULL);

  return tocItem->childCount;
}

EXPORT EPUB3Error EPUB3TocItemGetChildren(EPUB3TocItemRef parent, EPUB3TocItemRef *children)
{
  assert(parent != NULL);

  EPUB3Error error = kEPUB3Success;

  if(parent->childCount > 0) {
    int32_t count = 0;
    EPUB3TocItemChildListItemPtr itemPtr = parent->childrenHead;
    while(itemPtr != NULL) {
      children[count] = itemPtr->item;
      count++;
      itemPtr = itemPtr->next;
    }

  }

  return error;
}

EXPORT char * EPUB3TocItemCopyTitle(EPUB3TocItemRef tocItem)
{
  assert(tocItem != NULL);

  if(tocItem->title == NULL) return NULL;

  char * title = strdup(tocItem->title);
  return title;
}

EXPORT char * EPUB3TocItemCopyPath(EPUB3TocItemRef tocItem)
{
  assert(tocItem != NULL);

  if(tocItem->href == NULL) return NULL;

  return strdup(tocItem->href);
}

EXPORT EPUB3Error EPUB3CopyFileIntoBuffer_(EPUB3Ref epub, void **buffer, uint32_t *bufferSize, uint32_t *bytesCopied, const char * filename) {
    char *rootFilePath = NULL;
    EPUB3Error error = EPUB3CopyRootFilePathFromContainer(epub, &rootFilePath);
    if (error == kEPUB3Success) {
       char *rootPath = EPUB3CopyOfPathByDeletingLastPathComponent(rootFilePath);
       char *fullPath = EPUB3CopyOfPathByAppendingPathComponent(rootPath, filename);
       error = EPUB3CopyFileIntoBuffer(epub, buffer, bufferSize, bytesCopied, fullPath);
       EPUB3_FREE_AND_NULL(rootPath);
       EPUB3_FREE_AND_NULL(fullPath);

    }
  return error;
}

#pragma mark - Base Object

void EPUB3ObjectRelease(void *object)
{
  if(object == NULL) return;

  EPUB3ObjectRef obj = (EPUB3ObjectRef)object;
  obj->_type.refCount--;
  if(obj->_type.refCount == 0) {
    EPUB3_FREE_AND_NULL(obj);
  }
}

void EPUB3ObjectRetain(void *object)
{
  if(object == NULL) return;

  EPUB3ObjectRef obj = (EPUB3ObjectRef)object;
  obj->_type.refCount++;
}

void * EPUB3ObjectInitWithTypeID(void *object, const char *typeID)
{
  assert(object != NULL);
  EPUB3ObjectRef obj = (EPUB3ObjectRef)object;
  obj->_type.typeID = typeID;
  obj->_type.refCount = 1;
  return obj;
}

#pragma mark - Main EPUB3 Object

EPUB3Ref EPUB3Create()
{
  EPUB3Ref memory = malloc(sizeof(struct EPUB3));
  memory = EPUB3ObjectInitWithTypeID(memory, kEPUB3TypeID);
  memory->metadata = NULL;
  memory->manifest = NULL;
  memory->spine = NULL;
  memory->toc = NULL;
  memory->archive = NULL;
  memory->archivePath = NULL;
  memory->archiveFileCount = 0;
  return memory;
}

EXPORT EPUB3Ref EPUB3CreateWithArchiveAtPath(const char * path, EPUB3Error *error)
{
  assert(path != NULL);
  EPUB3Ref epub = EPUB3Create();
  *error = EPUB3PrepareArchiveAtPath(epub, path);
  if(*error != kEPUB3Success) {
    EPUB3Release(epub);
    return NULL;
  }
  *error = EPUB3InitAndValidate(epub);
  if(*error != kEPUB3Success) {
    EPUB3Release(epub);
    return NULL;
  }
  return epub;
}

EXPORT EPUB3Ref EPUB3CreateWithArchiveAtFd(const int fd, EPUB3Error *error)
{
    EPUB3Ref epub = EPUB3Create();
    *error = EPUB3PrepareArchiveAtFd(epub, fd);
    if(*error != kEPUB3Success) {
        EPUB3Release(epub);
        return NULL;
    }
    *error = EPUB3InitAndValidate(epub);
    if(*error != kEPUB3Success) {
        EPUB3Release(epub);
        return NULL;
    }
    return epub;
}

EPUB3Error EPUB3PrepareArchiveAtPath(EPUB3Ref epub, const char * path)
{
  assert(epub != NULL);
  assert(path != NULL);
  EPUB3Error error = kEPUB3Success;
  unzFile archive = unzOpen(path);
  if (archive != NULL)
  {
    epub->archive = archive;
    epub->archiveFileCount = EPUB3GetFileCountInArchive(archive);
    epub->archivePath = strdup(path);
  }
  else // unzOpen can return a NULL filestream
    error = kEPUB3UnknownError;
  return error;
}

EPUB3Error EPUB3PrepareArchiveAtFd(EPUB3Ref epub, const int fd)
{
    assert(epub != NULL);
    EPUB3Error error = kEPUB3Success;
    // 1. 初始化自定义 I/O 函数表
    zlib_filefunc64_def ffunc;
    ffunc.zopen64_file = fd_fopen;
    ffunc.zread_file = fd_fread;
    ffunc.zwrite_file = NULL; // 只读，解压不需要写
    ffunc.ztell64_file = fd_ftell;
    ffunc.zseek64_file = (seek64_file_func) fd_fseek;
    ffunc.zclose_file = fd_fclose;
    ffunc.zerror_file = fd_error;
    ffunc.opaque = (voidpf)(intptr_t)fd; // 将 fd 作为 opaque 传递，这样上面的回调函数就能用到它
    // 2. 使用自定义 I/O 打开 Zip 文件
    // 注意：第二个参数这里传入 NULL，因为我们的 fopen 回调忽略了文件名，只使用了 opaque (fd)
    unzFile archive = unzOpen2_64(NULL, &ffunc);

    if (archive != NULL)
    {
        epub->archive = archive;
        //epub->archiveFileCount = EPUB3GetFileCountInArchive(archive);
    }
    else // unzOpen can return a NULL filestream
        error = kEPUB3UnknownError;
    return error;
}

EPUB3Error EPUB3InitAndValidate(EPUB3Ref epub)
{
  assert(epub != NULL);
  char * opfPath = NULL;
  EPUB3Error error = EPUB3CopyRootFilePathFromContainer(epub, &opfPath);
  if(error != kEPUB3Success) {
    fprintf(stderr, "Error (%d[%d]) opening and validating epub file at %s.\n", error, __LINE__, epub->archivePath);
  }
  error = EPUB3InitFromOPF(epub, opfPath);
  if(error != kEPUB3Success) {
    fprintf(stderr, "Error (%d[%d]) parsing epub file at %s.\n", error, __LINE__, epub->archivePath);
  }
  EPUB3_FREE_AND_NULL(opfPath);
  return error;
}

EXPORT void EPUB3Retain(EPUB3Ref epub)
{
  if(epub == NULL) return;

  EPUB3MetadataRetain(epub->metadata);
  EPUB3ManifestRetain(epub->manifest);
  EPUB3SpineRetain(epub->spine);
  EPUB3ObjectRetain(epub);
}

EXPORT void EPUB3Release(EPUB3Ref epub)
{
  if(epub == NULL) return;

  if(epub->_type.refCount == 1) {
    if(epub->archive != NULL) {
      unzClose(epub->archive);
      epub->archive = NULL;
    }
    EPUB3_FREE_AND_NULL(epub->archivePath);
  }

  EPUB3MetadataRelease(epub->metadata);
  EPUB3ManifestRelease(epub->manifest);
  EPUB3SpineRelease(epub->spine);
  EPUB3ObjectRelease(epub);
}

EPUB3MetadataRef EPUB3CopyMetadata(EPUB3Ref epub)
{
  assert(epub != NULL);

  if(epub->metadata == NULL) {
    return NULL;
  }
  EPUB3MetadataRef copy = EPUB3MetadataCreate();
  copy->ncxItem = epub->metadata->ncxItem;
  EPUB3ManifestItemRetain(copy->ncxItem);
  (void)EPUB3MetadataSetTitle(copy, epub->metadata->title);
  (void)EPUB3MetadataSetAuthor(copy, epub->metadata->author);
  (void)EPUB3MetadataSetIdentifier(copy, epub->metadata->identifier);
  (void)EPUB3MetadataSetLanguage(copy, epub->metadata->language);
  return copy;
}

void EPUB3SetMetadata(EPUB3Ref epub, EPUB3MetadataRef metadata)
{
  assert(epub != NULL);

  if(epub->metadata != NULL) {
    EPUB3MetadataRelease(epub->metadata);
  }
  if(metadata != NULL) {
    EPUB3MetadataRetain(metadata);
  }
  epub->metadata = metadata;
}

void EPUB3SetManifest(EPUB3Ref epub, EPUB3ManifestRef manifest)
{
  assert(epub != NULL);

  if(epub->manifest != NULL) {
    EPUB3ManifestRelease(epub->manifest);
  }
  if(manifest != NULL) {
    EPUB3ManifestRetain(manifest);
  }
  epub->manifest = manifest;
}

void EPUB3SetSpine(EPUB3Ref epub, EPUB3SpineRef spine)
{
  assert(epub != NULL);

  if(epub->spine != NULL) {
    EPUB3SpineRelease(epub->spine);
  }
  if(spine != NULL) {
    EPUB3SpineRetain(spine);
  }
  epub->spine = spine;
}

void EPUB3SetStringValue(char ** location, const char *value)
{
  EPUB3_FREE_AND_NULL(*location);
  if(value == NULL) {
    return;
  }
  char * valueCopy = strdup(value);
  *location = valueCopy;
}

char * EPUB3CopyStringValue(char ** location)
{
  if(*location == NULL) return NULL;

  char * copy = strdup(*location);
  return copy;
}

EXPORT char * EPUB3CopyTitle(EPUB3Ref epub)
{
  assert(epub != NULL);
  assert(epub->metadata != NULL);
  return EPUB3CopyStringValue(&(epub->metadata->title));
}

EXPORT char * EPUB3CopyAuthor(EPUB3Ref epub)
{
    assert(epub != NULL);
    assert(epub->metadata != NULL);
    return EPUB3CopyStringValue(&(epub->metadata->author));
}

EXPORT char * EPUB3CopyIdentifier(EPUB3Ref epub)
{
  assert(epub != NULL);
  assert(epub->metadata != NULL);
  return EPUB3CopyStringValue(&(epub->metadata->identifier));
}

EXPORT char * EPUB3CopyLanguage(EPUB3Ref epub)
{
  assert(epub != NULL);
  assert(epub->metadata != NULL);
  return EPUB3CopyStringValue(&(epub->metadata->language));
}

EXPORT char * EPUB3CopyCoverImagePath(EPUB3Ref epub)
{
  assert(epub != NULL);
  assert(epub->metadata != NULL);
  assert(epub->manifest != NULL);

  if(epub->metadata->coverImageId == NULL) return NULL;

  EPUB3ManifestItemListItemPtr coverItemPtr = EPUB3ManifestFindItemWithId(epub->manifest, epub->metadata->coverImageId);
    return (coverItemPtr != NULL) ? EPUB3CopyStringValue(&(coverItemPtr->item->href)) : NULL;
}

EXPORT char * EPUB3CopyMetaElementContentWithName(EPUB3Ref epub, const char * name)
{
    assert(epub != NULL);
    assert(epub->metadata != NULL);

    char * contentValue = NULL;


    if (epub->metadata->itemCount > 0)
    {
        EPUB3MetadataMetaItemRef item = EPUB3MetadataFindItemWithId(epub->metadata, name);
        if (item != NULL)
        {
            contentValue = item->content;
        }
    }

    return contentValue;
}

EXPORT char * EPUB3CopyMetaElementPathWithName(EPUB3Ref epub, const char * name)
{
    assert(epub != NULL);
    assert(epub->metadata != NULL);
    assert(epub->manifest != NULL);

    char * fullPathCopy = NULL;

    if (epub->metadata->itemCount > 0)
    {
        EPUB3MetadataMetaItemRef item = EPUB3MetadataFindItemWithId(epub->metadata, name);
        if (item != NULL)
        {
            char * contentId = item->content;

            EPUB3ManifestItemListItemPtr manifestItem = EPUB3ManifestFindItemWithId(epub->manifest, contentId);
            if (manifestItem != NULL)
            {
                char * rootFilePath = NULL;
                EPUB3Error error = EPUB3CopyRootFilePathFromContainer(epub, &rootFilePath);
                if(error == kEPUB3Success)
                {
                    char * rootPath = EPUB3CopyOfPathByDeletingLastPathComponent(rootFilePath);
                    char * fullPath = EPUB3CopyOfPathByAppendingPathComponent(rootPath, manifestItem->item->href);
                    fullPathCopy = strdup(fullPath);

                    EPUB3_FREE_AND_NULL(rootPath);
                    EPUB3_FREE_AND_NULL(fullPath);
                }
                EPUB3_FREE_AND_NULL(rootFilePath);
            }

        }
    }

    return fullPathCopy;
}

EXPORT EPUB3Error EPUB3CopyCoverImage(EPUB3Ref epub, void ** bytes, uint32_t * byteCount)
{
  assert(epub != NULL);

  *byteCount = 0U;
  char * path = EPUB3CopyCoverImagePath(epub);
  if(path == NULL)
  {
      return kEPUB3FileNotFoundInArchiveError;
  }
  char * rootFilePath = NULL;
  EPUB3Error error = EPUB3CopyRootFilePathFromContainer(epub, &rootFilePath);
  if(error == kEPUB3Success) {
    char * rootPath = EPUB3CopyOfPathByDeletingLastPathComponent(rootFilePath);
    char * fullPath = EPUB3CopyOfPathByAppendingPathComponent(rootPath, path);
    error = EPUB3CopyFileIntoBuffer(epub, bytes, NULL, byteCount, fullPath);
    EPUB3_FREE_AND_NULL(rootPath);
    EPUB3_FREE_AND_NULL(fullPath);
  }
  EPUB3_FREE_AND_NULL(path);
  EPUB3_FREE_AND_NULL(rootFilePath);
  return error;
}

#pragma mark - Table of Contents

EPUB3TocRef EPUB3TocCreate()
{
  EPUB3TocRef memory = malloc(sizeof(struct EPUB3Toc));
  memory = EPUB3ObjectInitWithTypeID(memory, kEPUB3TocTypeID);
  memory->rootItemCount = 0;
  memory->rootItemsHead = NULL;
  memory->rootItemsTail = NULL;
  return memory;
}

void EPUB3TocRetain(EPUB3TocRef toc)
{
  if(toc == NULL) return;

  EPUB3TocItemChildListItemPtr itemPtr;
  for(itemPtr = toc->rootItemsHead; itemPtr != NULL; itemPtr = itemPtr->next) {
    EPUB3TocItemRetain(itemPtr->item);
  }
  EPUB3ObjectRetain(toc);
}

void EPUB3TocRelease(EPUB3TocRef toc)
{
  if(toc == NULL) return;
  if(toc->_type.refCount == 1) {
    EPUB3TocItemChildListItemPtr itemPtr = toc->rootItemsHead;
    int totalItemsToFree = toc->rootItemCount;
    while(itemPtr != NULL) {
      assert(--totalItemsToFree >= 0);
      EPUB3TocItemRelease(itemPtr->item);
      EPUB3TocItemChildListItemPtr tmp = itemPtr;
      itemPtr = itemPtr->next;
      toc->rootItemsHead = itemPtr;
      EPUB3_FREE_AND_NULL(tmp);
    }
    toc->rootItemCount = 0;
  }
  EPUB3ObjectRelease(toc);
}

EPUB3TocItemRef EPUB3TocItemCreate()
{
  EPUB3TocItemRef memory = malloc(sizeof(struct EPUB3TocItem));
  memory = EPUB3ObjectInitWithTypeID(memory, kEPUB3TocItemTypeID);
  memory->title = NULL;
  memory->href = NULL;
  memory->parent = NULL;
  memory->childCount = 0;
  memory->childrenHead = NULL;
  memory->childrenTail = NULL;
  return memory;
}

void EPUB3TocItemRetain(EPUB3TocItemRef item)
{
  if(item == NULL) return;
  EPUB3ObjectRetain(item);
}

void EPUB3TocItemRelease(EPUB3TocItemRef item)
{
  if(item == NULL) return;

  if(item->_type.refCount == 1) {
    item->parent = NULL; // zero weak ref
    EPUB3_FREE_AND_NULL(item->title);
    EPUB3_FREE_AND_NULL(item->href);
    int totalChildrenToFree = item->childCount;
    EPUB3TocItemChildListItemPtr itemPtr = item->childrenHead;
    while(itemPtr != NULL) {
      assert(--totalChildrenToFree >= 0);
      EPUB3TocItemRelease(itemPtr->item);
      EPUB3TocItemChildListItemPtr tmp = itemPtr;
      itemPtr = itemPtr->next;
      item->childrenHead = itemPtr;
      EPUB3_FREE_AND_NULL(tmp);
    }
    item->childCount = 0;
  }

  EPUB3ObjectRelease(item);
}

void EPUB3TocAddRootItem(EPUB3TocRef toc, EPUB3TocItemRef item)
{
  assert(toc != NULL);
  assert(item != NULL);

  EPUB3TocItemRetain(item);
  EPUB3TocItemChildListItemPtr itemPtr = (EPUB3TocItemChildListItemPtr) calloc(1, sizeof(struct EPUB3TocItemChildListItem));
  itemPtr->item = item;

  if(toc->rootItemsHead == NULL) {
    // First item
    toc->rootItemsHead = itemPtr;
    toc->rootItemsTail = itemPtr;
  } else {
    toc->rootItemsTail->next = itemPtr;
    toc->rootItemsTail = itemPtr;
  }
  toc->rootItemCount++;
}

void EPUB3TocItemAppendChild(EPUB3TocItemRef parent, EPUB3TocItemRef child)
{
  assert(parent != NULL);
  assert(child != NULL);

  EPUB3TocItemRetain(child);
  EPUB3TocItemChildListItemPtr itemPtr = (EPUB3TocItemChildListItemPtr) calloc(1, sizeof(struct EPUB3TocItemChildListItem));
  itemPtr->item = child;

  if(parent->childrenHead == NULL) {
    // First item
    parent->childrenHead = itemPtr;
    parent->childrenTail = itemPtr;
  } else {
    parent->childrenTail->next = itemPtr;
    parent->childrenTail = itemPtr;
  }
  child->parent = parent;
  parent->childCount++;
}

#pragma mark - Metadata

void EPUB3MetadataRetain(EPUB3MetadataRef metadata)
{
  if(metadata == NULL) return;

  EPUB3ManifestItemRetain(metadata->ncxItem);

    for(int i = 0; i < META_ITEM_HASH_SIZE; i++) {
        EPUB3MetadataMetaItemRef itemPtr = metadata->metaTable[i];
        if (itemPtr != NULL)
        {
            EPUB3MetadataMetaItemRetain(itemPtr);
        }
    }

  EPUB3ObjectRetain(metadata);
}

void EPUB3MetadataRelease(EPUB3MetadataRef metadata)
{
  if(metadata == NULL) return;

  if(metadata->_type.refCount == 1) {
    EPUB3ManifestItemRelease(metadata->ncxItem);
    metadata->ncxItem = NULL;
    EPUB3_FREE_AND_NULL(metadata->title);
    EPUB3_FREE_AND_NULL(metadata->_uniqueIdentifierID);
    EPUB3_FREE_AND_NULL(metadata->identifier);
    EPUB3_FREE_AND_NULL(metadata->language);
    EPUB3_FREE_AND_NULL(metadata->coverImageId);

      for(int i = 0; i < META_ITEM_HASH_SIZE; i++) {

          EPUB3MetadataMetaItemRef next = metadata->metaTable[i];
          EPUB3_FREE_AND_NULL(next);
          metadata->metaTable[i] = NULL;
      }
      metadata->itemCount = 0;
  }
  EPUB3ObjectRelease(metadata);
}

void EPUB3MetadataMetaItemRetain(EPUB3MetadataMetaItemRef item)
{
    if(item == NULL) return;

    EPUB3ObjectRetain(item);
}

void EPUB3MetadataMetaItemRelease(EPUB3MetadataMetaItemRef item)
{
    if(item == NULL) return;

    if(item->_type.refCount == 1) {
        EPUB3_FREE_AND_NULL(item->name);
        EPUB3_FREE_AND_NULL(item->content);
    }

    EPUB3ObjectRelease(item);
}

EPUB3MetadataRef EPUB3MetadataCreate()
{
  EPUB3MetadataRef memory = malloc(sizeof(struct EPUB3Metadata));
  memory = EPUB3ObjectInitWithTypeID(memory, kEPUB3MetadataTypeID);
  memory->ncxItem = NULL;
  memory->title = NULL;
  memory->author = NULL;
  memory->_uniqueIdentifierID = NULL;
  memory->identifier = NULL;
  memory->language = NULL;
  memory->coverImageId = NULL;
  memory->itemCount = 0;
  for(int i = 0; i < META_ITEM_HASH_SIZE; i++) {
      memory->metaTable[i] = NULL;
  }
  return memory;
}

EPUB3MetadataMetaItemRef EPUB3MetadataItemCreate()
{
    EPUB3MetadataMetaItemRef memory = malloc(sizeof(struct EPUB3MetadataMetaItem));
    memory = EPUB3ObjectInitWithTypeID(memory, kEPUB3MetadataItemTypeID);
    memory->name = NULL;
    memory->content = NULL;
    return memory;
}

void EPUB3MetadataInsertItem(EPUB3MetadataRef metadata, EPUB3MetadataMetaItemRef item)
{
    assert(metadata != NULL);
    assert(item != NULL);
    assert(item->name != NULL);

    EPUB3MetadataMetaItemRetain(item);
    EPUB3MetadataMetaItemRef itemPtr = EPUB3MetadataFindItemWithId(metadata, item->name);
    if(itemPtr == NULL) {
        itemPtr = (EPUB3MetadataMetaItemRef) malloc(sizeof(struct EPUB3MetadataMetaItem));
        if (metadata->itemCount < META_ITEM_HASH_SIZE)
        {
            itemPtr->name = item->name;
            itemPtr->content = item->content;
            metadata->metaTable[metadata->itemCount++] = itemPtr;
        }
    } else {
        EPUB3MetadataMetaItemRelease(itemPtr);
        itemPtr = item;
    }
}

EPUB3MetadataMetaItemRef EPUB3MetadataCopyItemWithId(EPUB3MetadataRef metadata, const char * itemId)
{
    assert(metadata != NULL);
    assert(itemId != NULL);

    EPUB3MetadataMetaItemRef itemPtr = EPUB3MetadataFindItemWithId(metadata, itemId);

    if(itemPtr == NULL) {
        return NULL;
    }

    EPUB3MetadataMetaItemRef item = itemPtr;
    EPUB3MetadataMetaItemRef copy = EPUB3MetadataItemCreate();
    copy->name = item->name != NULL ? strdup(item->name) : NULL;
    copy->content = item->content != NULL ? strdup(item->content) : NULL;
    return copy;
}

EPUB3MetadataMetaItemRef EPUB3MetadataFindItemWithId(EPUB3MetadataRef metadata, const char * itemId) // name
{
    assert(metadata != NULL);
    assert(itemId != NULL);

    for (int idx=0; idx < META_ITEM_HASH_SIZE; idx++)
    {
        EPUB3MetadataMetaItemRef itemPtr = metadata->metaTable[idx];
        if (itemPtr != NULL && strcmp(itemPtr->name, itemId) == 0)
            return itemPtr;
    }
    return NULL;
}

void EPUB3MetadataSetNCXItem(EPUB3MetadataRef metadata, EPUB3ManifestItemRef ncxItem)
{
  assert(metadata != NULL);

  if(metadata->ncxItem != NULL) {
    EPUB3ManifestItemRelease(metadata->ncxItem);
  }
  EPUB3ManifestItemRetain(ncxItem);
  metadata->ncxItem = ncxItem;
}

void EPUB3MetadataSetTitle(EPUB3MetadataRef metadata, const char * title)
{
  assert(metadata != NULL);
  EPUB3SetStringValue(&(metadata->title), title);
}
void EPUB3MetadataSetAuthor(EPUB3MetadataRef metadata, const char * author)
{
  assert(metadata != NULL);
  EPUB3SetStringValue(&(metadata->author), author);
}

void EPUB3MetadataSetIdentifier(EPUB3MetadataRef metadata, const char * identifier)
{
  assert(metadata != NULL);
  EPUB3SetStringValue(&(metadata->identifier), identifier);
}

void EPUB3MetadataSetLanguage(EPUB3MetadataRef metadata, const char * language)
{
  assert(metadata != NULL);
  EPUB3SetStringValue(&(metadata->language), language);
}

void EPUB3MetadataSetCoverImageId(EPUB3MetadataRef metadata, const char * coverImgId)
{
  assert(metadata != NULL);
  EPUB3SetStringValue(&(metadata->coverImageId), coverImgId);
}

#pragma mark - Manifest

void EPUB3ManifestRetain(EPUB3ManifestRef manifest)
{
  if(manifest == NULL) return;

  for(int i = 0; i < MANIFEST_HASH_SIZE; i++) {
    EPUB3ManifestItemListItemPtr itemPtr = manifest->itemTable[i];
    while(itemPtr != NULL) {
      EPUB3ManifestItemRetain(itemPtr->item);
      itemPtr = itemPtr->next;
    }
  }
  EPUB3ObjectRetain(manifest);
}

void EPUB3ManifestRelease(EPUB3ManifestRef manifest)
{
  if(manifest == NULL) return;
  for(int i = 0; i < MANIFEST_HASH_SIZE; i++) {

    EPUB3ManifestItemListItemPtr next = manifest->itemTable[i];
    while(next != NULL) {
      EPUB3ManifestItemRelease(next->item);
      EPUB3ManifestItemListItemPtr tmp = next;
      next = tmp->next;
      if(manifest->_type.refCount == 1) {
        EPUB3_FREE_AND_NULL(tmp);
      }
    }
    if(manifest->_type.refCount == 1) {
      manifest->itemTable[i] = NULL;
    }
  }
  if(manifest->_type.refCount == 1) {
    manifest->itemCount = 0;
  }
  EPUB3ObjectRelease(manifest);
}

void EPUB3ManifestItemRetain(EPUB3ManifestItemRef item)
{
  if(item == NULL) return;

  EPUB3ObjectRetain(item);
}

void EPUB3ManifestItemRelease(EPUB3ManifestItemRef item)
{
  if(item == NULL) return;

  if(item->_type.refCount == 1) {
    EPUB3_FREE_AND_NULL(item->itemId);
    EPUB3_FREE_AND_NULL(item->href);
    EPUB3_FREE_AND_NULL(item->mediaType);
    EPUB3_FREE_AND_NULL(item->properties);
    EPUB3_FREE_AND_NULL(item->requiredModules);
  }

  EPUB3ObjectRelease(item);
}

EPUB3ManifestRef EPUB3ManifestCreate()
{
  EPUB3ManifestRef memory = malloc(sizeof(struct EPUB3Manifest));
  memory = EPUB3ObjectInitWithTypeID(memory, kEPUB3ManifestTypeID);
  memory->itemCount = 0;
  for(int i = 0; i < MANIFEST_HASH_SIZE; i++) {
    memory->itemTable[i] = NULL;
  }
  return memory;
}

EPUB3ManifestItemRef EPUB3ManifestItemCreate()
{
  EPUB3ManifestItemRef memory = malloc(sizeof(struct EPUB3ManifestItem));
  memory = EPUB3ObjectInitWithTypeID(memory, kEPUB3ManifestItemTypeID);
  memory->itemId = NULL;
  memory->href = NULL;
  memory->mediaType = NULL;
  memory->properties = NULL;
  memory->requiredModules = NULL;
  return memory;
}

void EPUB3ManifestInsertItem(EPUB3ManifestRef manifest, EPUB3ManifestItemRef item)
{
  assert(manifest != NULL);
  assert(item != NULL);
  assert(item->itemId != NULL);

  EPUB3ManifestItemRetain(item);
  EPUB3ManifestItemListItemPtr itemPtr = EPUB3ManifestFindItemWithId(manifest, item->itemId);
  if(itemPtr == NULL) {
    itemPtr = (EPUB3ManifestItemListItemPtr) malloc(sizeof(struct EPUB3ManifestItemListItem));
    int32_t bucket = SuperFastHash(item->itemId, (int32_t)strlen(item->itemId)) % MANIFEST_HASH_SIZE;
    itemPtr->item = item;
    itemPtr->next = manifest->itemTable[bucket];
    manifest->itemTable[bucket] = itemPtr;
    manifest->itemCount++;
  } else {
    EPUB3ManifestItemRelease(itemPtr->item);
    itemPtr->item = item;
  }
}

EPUB3ManifestItemRef EPUB3ManifestCopyItemWithId(EPUB3ManifestRef manifest, const char * itemId)
{
  assert(manifest != NULL);
  assert(itemId != NULL);

  EPUB3ManifestItemListItemPtr itemPtr = EPUB3ManifestFindItemWithId(manifest, itemId);

  if(itemPtr == NULL) {
    return NULL;
  }

  EPUB3ManifestItemRef item = itemPtr->item;
  EPUB3ManifestItemRef copy = EPUB3ManifestItemCreate();
  copy->itemId = item->itemId != NULL ? strdup(item->itemId) : NULL;
  copy->href = item->href != NULL ? strdup(item->href) : NULL;
  copy->mediaType = item->mediaType != NULL ? strdup(item->mediaType) : NULL;
  copy->properties = item->properties != NULL ? strdup(item->properties) : NULL;
  copy->requiredModules = item->requiredModules != NULL ? strdup(item->requiredModules) : NULL;
  return copy;
}

EPUB3ManifestItemListItemPtr EPUB3ManifestFindItemWithId(EPUB3ManifestRef manifest, const char * itemId)
{
  assert(manifest != NULL);
  assert(itemId != NULL);

  int32_t bucket = SuperFastHash(itemId, (int32_t)strlen(itemId)) % MANIFEST_HASH_SIZE;
  EPUB3ManifestItemListItemPtr itemPtr = manifest->itemTable[bucket];
  while(itemPtr != NULL) {
    if(strcmp(itemId, itemPtr->item->itemId) == 0) {
      return itemPtr;
    }
    itemPtr = itemPtr->next;
  }
  return NULL;
}

EXPORT void EPUB3ManifestFindItemsMatchingRequiredModuleWithName(EPUB3Ref epub, const char * moduleName, char ** matchingItems, int32_t matchSize)
{
    assert(epub != NULL);

    int32_t matchCount = 0, manifestItemCount = 0;
    EPUB3ManifestItemListItemPtr itemPtr;

    while (manifestItemCount < MANIFEST_HASH_SIZE)
    {
        do {
            itemPtr = epub->manifest->itemTable[manifestItemCount++];
        } while (itemPtr == NULL && manifestItemCount < MANIFEST_HASH_SIZE-1);

        while (itemPtr != NULL && matchCount < matchSize) {
            char * modules = itemPtr->item->requiredModules;
            if (modules != NULL && strcmp(moduleName, itemPtr->item->requiredModules) == 0)
                matchingItems[matchCount++] = strdup(itemPtr->item->href);

            itemPtr = itemPtr->next;
        }
    }
}

#pragma mark - Spine

EPUB3SpineRef EPUB3SpineCreate()
{
  EPUB3SpineRef memory = malloc(sizeof(struct EPUB3Spine));
  memory = EPUB3ObjectInitWithTypeID(memory, kEPUB3SpineTypeID);
  memory->itemCount = 0;
  memory->linearItemCount = 0;
  memory->head = NULL;
  memory->tail = NULL;
  return memory;
}

void EPUB3SpineRetain(EPUB3SpineRef spine)
{
  if(spine == NULL) return;

  EPUB3SpineItemListItemPtr itemPtr;
  for(itemPtr = spine->head; itemPtr != NULL; itemPtr = itemPtr->next) {
    EPUB3SpineItemRetain(itemPtr->item);
  }
  EPUB3ObjectRetain(spine);
}

void EPUB3SpineRelease(EPUB3SpineRef spine)
{
  if(spine == NULL) return;
  if(spine->_type.refCount == 1) {
    EPUB3SpineItemListItemPtr itemPtr = spine->head;
    int totalItemsToFree = spine->itemCount;
    while(itemPtr != NULL) {
      assert(--totalItemsToFree >= 0);
      EPUB3SpineItemRelease(itemPtr->item);
      EPUB3SpineItemListItemPtr tmp = itemPtr;
      itemPtr = itemPtr->next;
      spine->head = itemPtr;
      EPUB3_FREE_AND_NULL(tmp);
    }
    spine->itemCount = 0;
    spine->linearItemCount = 0;
  }
  EPUB3ObjectRelease(spine);
}

EPUB3SpineItemRef EPUB3SpineItemCreate()
{
  EPUB3SpineItemRef memory = malloc(sizeof(struct EPUB3SpineItem));
  memory = EPUB3ObjectInitWithTypeID(memory, kEPUB3SpineItemTypeID);
  memory->isLinear = kEPUB3_NO;
  memory->idref = NULL;
  memory->manifestItem = NULL;
  return memory;
}

void EPUB3SpineItemRetain(EPUB3SpineItemRef item)
{
  if(item == NULL) return;
  EPUB3ObjectRetain(item);
}

void EPUB3SpineItemRelease(EPUB3SpineItemRef item)
{
  if(item == NULL) return;

  if(item->_type.refCount == 1) {
    item->manifestItem = NULL; // zero weak ref
    EPUB3_FREE_AND_NULL(item->idref);
  }

  EPUB3ObjectRelease(item);
}

void EPUB3SpineItemSetManifestItem(EPUB3SpineItemRef spineItem, EPUB3ManifestItemRef manifestItem)
{
  assert(spineItem != NULL);
  spineItem->manifestItem = manifestItem;
  spineItem->idref = strdup(manifestItem->itemId);
}

void EPUB3SpineAppendItem(EPUB3SpineRef spine, EPUB3SpineItemRef item)
{
  assert(spine != NULL);
  assert(item != NULL);

  EPUB3SpineItemRetain(item);
  EPUB3SpineItemListItemPtr itemPtr = (EPUB3SpineItemListItemPtr) calloc(1, sizeof(struct EPUB3SpineItemListItem));
  itemPtr->item = item;

  if(spine->head == NULL) {
    // First item
    spine->head = itemPtr;
    spine->tail = itemPtr;
  } else {
    spine->tail->next = itemPtr;
    spine->tail = itemPtr;
  }
  spine->itemCount++;
}

#pragma mark - OPF XML Parsing

EPUB3Error EPUB3InitFromOPF(EPUB3Ref epub, const char * opfFilename)
{
  assert(epub != NULL);
  assert(opfFilename != NULL);

  if(epub->archive == NULL) return kEPUB3ArchiveUnavailableError;

  if(epub->metadata == NULL) {
    epub->metadata = EPUB3MetadataCreate();
  }

  if(epub->manifest == NULL) {
    epub->manifest = EPUB3ManifestCreate();
  }

  if(epub->spine == NULL) {
    epub->spine = EPUB3SpineCreate();
  }

  if(epub->toc == NULL) {
    epub->toc = EPUB3TocCreate();
  }

  void *buffer = NULL;
  uint32_t bufferSize = 0;
  uint32_t bytesCopied;

  EPUB3Error error = kEPUB3Success;

  error = EPUB3CopyFileIntoBuffer(epub, &buffer, &bufferSize, &bytesCopied, opfFilename);
  if(error == kEPUB3Success) {
    error = EPUB3ParseOPFFromData(epub, buffer, bufferSize);
    EPUB3_FREE_AND_NULL(buffer);
  }
    if(error == kEPUB3Success) { //&& epub->metadata->version == kEPUB3Version_2) {
    // Parse NCX only if this is a v2 epub (per the EPUB 3 spec)
    if(epub->metadata->ncxItem != NULL) {
      char * ncxPath = strdup(epub->metadata->ncxItem->href);
      if(*ncxPath != '/') {
        char * opfRoot = EPUB3CopyOfPathByDeletingLastPathComponent(opfFilename);
        char * fullPath = EPUB3CopyOfPathByAppendingPathComponent(opfRoot, ncxPath);
        free(ncxPath);
        free(opfRoot);
        ncxPath = fullPath;
      }
      bufferSize = 0;
      error = EPUB3CopyFileIntoBuffer(epub, &buffer, &bufferSize, &bytesCopied, ncxPath);
      if(error == kEPUB3Success) {
        error = EPUB3ParseNCXFromData(epub, buffer, bufferSize);
      }
      free(ncxPath);
      EPUB3_FREE_AND_NULL(buffer);
    }
  }
  return error;
}

void _EPUB3DumpXMLParseContextStack(EPUB3XMLParseContextPtr *ctxPtr)
{
  EPUB3XMLParseContextPtr top = *ctxPtr;
  fprintf(stderr, "== Parse Context Stack ==\n");
  for(;;) {
    fprintf(stderr, "%s\n", (const char *)top->tagName);
    if(top->state == kEPUB3NCXStateRoot || top->state == kEPUB3OPFStateRoot) break;
    top--;
  }
  fprintf(stderr, "== END Context Stack ==\n");
}

void EPUB3SaveParseContext(EPUB3XMLParseContextPtr *ctxPtr, EPUB3XMLParseState state, const xmlChar * tagName, int32_t attrCount, char ** attrs, EPUB3Bool shouldParseTextNode, void * userInfo)
{
  (*ctxPtr)++;
  (*ctxPtr)->state = state;
  (*ctxPtr)->tagName = tagName;
  (*ctxPtr)->attributeCount = attrCount;
  (*ctxPtr)->attributes = attrs;
  (*ctxPtr)->shouldParseTextNode = shouldParseTextNode;
  (*ctxPtr)->userInfo = userInfo;
}

void EPUB3PopAndFreeParseContext(EPUB3XMLParseContextPtr *contextPtr)
{
  EPUB3XMLParseContextPtr ctx = (*contextPtr);
  (*contextPtr)--;
  for (int i = 0; i < ctx->attributeCount; i++) {
    char * key = ctx->attributes[i * 2];
    char * val = ctx->attributes[i * 2 + 1];
    EPUB3_FREE_AND_NULL(key);
    EPUB3_FREE_AND_NULL(val);
  }
}

EPUB3Error EPUB3ProcessXMLReaderNodeForMetadataInOPF(EPUB3Ref epub, xmlTextReaderPtr reader, EPUB3XMLParseContextPtr *context)
{
  assert(epub != NULL);
  assert(reader != NULL);

  EPUB3Error error = kEPUB3Success;
  const xmlChar *name = xmlTextReaderConstLocalName(reader);
  xmlReaderTypes nodeType = xmlTextReaderNodeType(reader);

  switch(nodeType)
  {
    case XML_READER_TYPE_ELEMENT:
    {
      if(!xmlTextReaderIsEmptyElement(reader)) {
        (void)EPUB3SaveParseContext(context, kEPUB3OPFStateMetadata, name, 0, NULL, kEPUB3_YES, NULL);

        // Only parse text node for the identifier marked as unique-identifier in the package tag
        // see: http://idpf.org/epub/30/spec/epub30-publications.html#sec-opf-dcidentifier
        if(xmlStrcmp(name, BAD_CAST "identifier") == 0) {
          if(xmlTextReaderHasAttributes(reader)) {
            xmlChar * itemId = xmlTextReaderGetAttribute(reader, BAD_CAST "id");
            if(itemId == NULL) {
              (*context)->shouldParseTextNode = kEPUB3_NO;
            }
            else if(itemId != NULL && xmlStrcmp(itemId, BAD_CAST epub->metadata->_uniqueIdentifierID) != 0) {
              (*context)->shouldParseTextNode = kEPUB3_NO;
            }
            EPUB3_XML_FREE_AND_NULL(itemId);
          }
          break;
        }
      }
        if(xmlStrcmp(name, BAD_CAST "meta") == 0) {
          if(xmlTextReaderHasAttributes(reader)) {
            xmlChar * metaName = xmlTextReaderGetAttribute(reader, BAD_CAST "name");
            if (metaName != NULL)
            {
                if (epub->metadata->version == kEPUB3Version_2 && xmlStrcmp(metaName, BAD_CAST "cover") == 0)
                {
                    // There is no standard for cover images in EPUB 2. This is the accepted ad hoc method for defining one
                    // http://blog.threepress.org/2009/11/20/best-practices-in-epub-cover-images/
                    // http://www.mobipocket.com/dev/article.asp?BaseFolder=prcgen&File=cover.htm#IDPF2
                    xmlChar * coverId = xmlTextReaderGetAttribute(reader, BAD_CAST "content");
                    EPUB3MetadataSetCoverImageId(epub->metadata, (const char *)coverId);
                    EPUB3_XML_FREE_AND_NULL(coverId);
                }
                else
                {
                    /*
                     One or more optional instances of a meta element, analogous to the XHTML 1.1 meta element but applicable to the publication as a whole, may be placed within the metadata element or within the deprecated x-metadata element. This allows content providers to express arbitrary metadata beyond the data described by the Dublin Core specification. Individual OPS Content Documents may include the meta element directly (as in XHTML 1.1) for document-specific metadata. This specification uses the OPF Package Document alone as the basis for expressing publication-level Dublin Core metadata.
                     */
                    EPUB3MetadataMetaItemRef newItem = EPUB3MetadataItemCreate();
                    newItem->name = strdup((char *)metaName);
                    newItem->content = (char *)xmlTextReaderGetAttribute(reader, BAD_CAST "content");

                    EPUB3MetadataInsertItem(epub->metadata, newItem);
                }
            }
            EPUB3_XML_FREE_AND_NULL(metaName);
          }
        }
      break;
    }
    case XML_READER_TYPE_TEXT:
    {
      const xmlChar *value = xmlTextReaderValue(reader);
      if(value != NULL && (*context)->shouldParseTextNode) {
        if(xmlStrcmp((*context)->tagName, BAD_CAST "title") == 0) {
          (void)EPUB3MetadataSetTitle(epub->metadata, (const char *)value);
        }
        else if(xmlStrcmp((*context)->tagName, BAD_CAST "identifier") == 0) {
          (void)EPUB3MetadataSetIdentifier(epub->metadata, (const char *)value);
        }
        else if(xmlStrcmp((*context)->tagName, BAD_CAST "language") == 0) {
          (void)EPUB3MetadataSetLanguage(epub->metadata, (const char *)value);
        }
        else if(xmlStrcmp((*context)->tagName, BAD_CAST "creator") == 0) {
          (void)EPUB3MetadataSetAuthor(epub->metadata, (const char *)value);
        }
      }
      break;
    }
    case XML_READER_TYPE_END_ELEMENT:
    {
      (void)EPUB3PopAndFreeParseContext(context);
      break;
    }
    default: break;
  }
  return error;
}

EPUB3Error EPUB3ProcessXMLReaderNodeForManifestInOPF(EPUB3Ref epub, xmlTextReaderPtr reader, EPUB3XMLParseContextPtr *context)
{
  assert(epub != NULL);
  assert(reader != NULL);

  EPUB3Error error = kEPUB3Success;
  const xmlChar *name = xmlTextReaderConstLocalName(reader);
  xmlReaderTypes nodeType = xmlTextReaderNodeType(reader);

  switch(nodeType)
  {
    case XML_READER_TYPE_ELEMENT:
    {
      if(!xmlTextReaderIsEmptyElement(reader)) {
        (void)EPUB3SaveParseContext(context, kEPUB3OPFStateManifest, name, 0, NULL, kEPUB3_YES, NULL);
      } else {
        if(xmlStrcmp(name, BAD_CAST "item") == 0) {
          EPUB3ManifestItemRef newItem = EPUB3ManifestItemCreate();
          newItem->itemId = (char *)xmlTextReaderGetAttribute(reader, BAD_CAST "id");
          newItem->href = (char *)xmlTextReaderGetAttribute(reader, BAD_CAST "href");
          newItem->mediaType = (char *)xmlTextReaderGetAttribute(reader, BAD_CAST "media-type");
          newItem->properties = (char *)xmlTextReaderGetAttribute(reader, BAD_CAST "properties");
          newItem->requiredModules = (char*)xmlTextReaderGetAttribute(reader, BAD_CAST "required-modules");

          if(newItem->properties != NULL) {
            // Look for the cover-image property
            char *prop, *props, *tofree;
            tofree = props = strdup(newItem->properties);
            while((prop = strsep(&props, " ")) != NULL) {
              if(strcmp(prop, "cover-image") == 0) {
                EPUB3MetadataSetCoverImageId(epub->metadata, newItem->itemId);
              }
            }
            EPUB3_FREE_AND_NULL(tofree);
          }
          if(newItem->mediaType != NULL && strcmp(newItem->mediaType, "application/x-dtbncx+xml") == 0)
          {
            //This is the ref for the ncx document. Set it for v2 epubs
            //if(epub->metadata->version == kEPUB3Version_2) {
              EPUB3MetadataSetNCXItem(epub->metadata, newItem);
            //}
          }
          EPUB3ManifestInsertItem(epub->manifest, newItem);
        }
      }
      break;
    }
    case XML_READER_TYPE_TEXT:
    {
      break;
    }
    case XML_READER_TYPE_END_ELEMENT:
    {
      (void)EPUB3PopAndFreeParseContext(context);
      break;
    }
    default: break;
  }
  return error;
}

EPUB3Error EPUB3ProcessXMLReaderNodeForSpineInOPF(EPUB3Ref epub, xmlTextReaderPtr reader, EPUB3XMLParseContextPtr *context)
{
  assert(epub != NULL);
  assert(reader != NULL);

  EPUB3Error error = kEPUB3Success;
  const xmlChar *name = xmlTextReaderConstLocalName(reader);
  xmlReaderTypes nodeType = xmlTextReaderNodeType(reader);

  switch(nodeType)
  {
    case XML_READER_TYPE_ELEMENT:
    {
      if(!xmlTextReaderIsEmptyElement(reader)) {
        (void)EPUB3SaveParseContext(context, kEPUB3OPFStateManifest, name, 0, NULL, kEPUB3_YES, NULL);
      } else {
        if(xmlStrcmp(name, BAD_CAST "itemref") == 0) {
          EPUB3SpineItemRef newItem = EPUB3SpineItemCreate();
          xmlChar * linear = xmlTextReaderGetAttribute(reader, BAD_CAST "linear");

          if(linear == NULL || xmlStrcmp(linear, BAD_CAST "yes") == 0) {
            newItem->isLinear = kEPUB3_YES;
            epub->spine->linearItemCount++;
          }
          EPUB3_XML_FREE_AND_NULL(linear);
          newItem->idref = (char *)xmlTextReaderGetAttribute(reader, BAD_CAST "idref");
          if(newItem->idref != NULL) {
            EPUB3ManifestItemListItemPtr manifestPtr = EPUB3ManifestFindItemWithId(epub->manifest, newItem->idref);
            if(manifestPtr == NULL) {
              newItem->manifestItem = NULL;
            } else {
              newItem->manifestItem = manifestPtr->item;
            }
          }
          EPUB3SpineAppendItem(epub->spine, newItem);
        }
      }
      break;
    }
    case XML_READER_TYPE_TEXT:
    {
      break;
    }
    case XML_READER_TYPE_END_ELEMENT:
    {
      (void)EPUB3PopAndFreeParseContext(context);
      break;
    }
    default: break;
  }
  return error;
}

EPUB3Error EPUB3ParseXMLReaderNodeForOPF(EPUB3Ref epub, xmlTextReaderPtr reader, EPUB3XMLParseContextPtr *currentContext)
{
  assert(epub != NULL);
  assert(reader != NULL);
  assert(*currentContext != NULL);

  EPUB3Error error = kEPUB3Success;
  const xmlChar *name = xmlTextReaderConstLocalName(reader);
  xmlReaderTypes currentNodeType = xmlTextReaderNodeType(reader);

  if(name != NULL && currentNodeType != XML_READER_TYPE_COMMENT) {
    switch((*currentContext)->state)
    {
      case kEPUB3OPFStateRoot:
      {
//        fprintf(stdout, "ROOT: %s\n", name);
        if(currentNodeType == XML_READER_TYPE_ELEMENT) {
          if(xmlStrcmp(name, BAD_CAST "package") == 0 && xmlTextReaderHasAttributes(reader)) {
            EPUB3_FREE_AND_NULL(epub->metadata->_uniqueIdentifierID);
            epub->metadata->_uniqueIdentifierID = (char *)xmlTextReaderGetAttribute(reader, BAD_CAST "unique-identifier");
            xmlChar *versionString = xmlTextReaderGetAttribute(reader, BAD_CAST "version");
            if(versionString != NULL) {
              if(*versionString == '2') {
                epub->metadata->version = kEPUB3Version_2;
              } else if(*versionString == '3') {
                epub->metadata->version = kEPUB3Version_3;
              }
              EPUB3_XML_FREE_AND_NULL(versionString);
            }
          }
          else if(xmlStrcmp(name, BAD_CAST "metadata") == 0) {
            (void)EPUB3SaveParseContext(currentContext, kEPUB3OPFStateMetadata, name, 0, NULL, kEPUB3_YES, NULL);
          }
          else if(xmlStrcmp(name, BAD_CAST "manifest") == 0) {
            (void)EPUB3SaveParseContext(currentContext, kEPUB3OPFStateManifest, name, 0, NULL, kEPUB3_YES, NULL);
          }
          else if(xmlStrcmp(name, BAD_CAST "spine") == 0) {
            (void)EPUB3SaveParseContext(currentContext, kEPUB3OPFStateSpine, name, 0, NULL, kEPUB3_YES, NULL);
          }
        }
        break;
      }
      case kEPUB3OPFStateMetadata:
      {
//        fprintf(stdout, "METADATA: %s\n", name);
        if(currentNodeType == XML_READER_TYPE_END_ELEMENT && xmlStrcmp(name, BAD_CAST "metadata") == 0) {
          (void)EPUB3PopAndFreeParseContext(currentContext);
        } else {
          error = EPUB3ProcessXMLReaderNodeForMetadataInOPF(epub, reader, currentContext);
        }
        break;
      }
      case kEPUB3OPFStateManifest:
      {
//        fprintf(stdout, "MANIFEST: %s\n", name);
        if(currentNodeType == XML_READER_TYPE_END_ELEMENT && xmlStrcmp(name, BAD_CAST "manifest") == 0) {
          (void)EPUB3PopAndFreeParseContext(currentContext);
        } else {
          error = EPUB3ProcessXMLReaderNodeForManifestInOPF(epub, reader, currentContext);
        }
        break;
      }
      case kEPUB3OPFStateSpine:
      {
//        fprintf(stdout, "SPINE: %s\n", name);
        if(currentNodeType == XML_READER_TYPE_END_ELEMENT && xmlStrcmp(name, BAD_CAST "spine") == 0) {
          (void)EPUB3PopAndFreeParseContext(currentContext);
        } else {
          error = EPUB3ProcessXMLReaderNodeForSpineInOPF(epub, reader, currentContext);
        }
        break;
      }
      default: break;
    }
  }
  return error;
}

EPUB3Error EPUB3ParseOPFFromData(EPUB3Ref epub, void * buffer, uint32_t bufferSize)
{
  assert(epub != NULL);
  assert(buffer != NULL);
  assert(bufferSize > 0);

  EPUB3Error error = kEPUB3Success;
  xmlInitParser();
  xmlTextReaderPtr reader = NULL;
  reader = xmlReaderForMemory(buffer, bufferSize, NULL, NULL, XML_PARSE_RECOVER | XML_PARSE_NONET);
  if(reader != NULL) {
    EPUB3XMLParseContext contextStack[PARSE_CONTEXT_STACK_DEPTH];
    EPUB3XMLParseContextPtr currentContext = &contextStack[0];

    int retVal = xmlTextReaderRead(reader);
    currentContext->state = kEPUB3OPFStateRoot;
    currentContext->tagName = xmlTextReaderConstName(reader);
    while(retVal == 1)
    {
      error = EPUB3ParseXMLReaderNodeForOPF(epub, reader, &currentContext);
      retVal = xmlTextReaderRead(reader);
    }
    if(retVal < 0) {
      error = kEPUB3XMLParseError;
    }
  } else {
    error = kEPUB3XMLReadFromBufferError;
  }
  xmlFreeTextReader(reader);
  xmlCleanupParser();
  return error;
}

#pragma mark - NCX XML Parsing

// TODO: Refactor: This function differs from EPUB3ParseOPFFromData by 1 line.
EPUB3Error EPUB3ParseNCXFromData(EPUB3Ref epub, void * buffer, uint32_t bufferSize)
{
  assert(epub != NULL);
  assert(buffer != NULL);
  assert(bufferSize > 0);

  EPUB3Error error = kEPUB3Success;
  xmlInitParser();
  xmlTextReaderPtr reader = NULL;
  reader = xmlReaderForMemory(buffer, bufferSize, NULL, NULL, XML_PARSE_RECOVER | XML_PARSE_NONET);
  if(reader != NULL) {
    EPUB3XMLParseContext contextStack[PARSE_CONTEXT_NCX_STACK_DEPTH];
    EPUB3XMLParseContextPtr currentContext = &contextStack[0];

    int retVal = xmlTextReaderRead(reader);
    currentContext->state = kEPUB3NCXStateRoot;
    currentContext->tagName = xmlTextReaderConstName(reader);
    while(retVal == 1)
    {
//      _EPUB3DumpXMLParseContextStack(&currentContext);
        error = EPUB3ParseXMLReaderNodeForNCX(epub, reader, &currentContext);
        if (error != kEPUB3NCXNavMapEnd && reader != NULL) {
            retVal = xmlTextReaderRead(reader);
        }
        else {
            retVal = 0;
            error = kEPUB3Success;
        }
    }
    if(retVal < 0) {
      error = kEPUB3XMLParseError;
    }
  } else {
    error = kEPUB3XMLReadFromBufferError;
  }
  xmlFreeTextReader(reader);
  xmlCleanupParser();
  return error;
}

EPUB3Error EPUB3ParseXMLReaderNodeForNCX(EPUB3Ref epub, xmlTextReaderPtr reader, EPUB3XMLParseContextPtr *currentContext)
{
  assert(epub != NULL);
  assert(reader != NULL);
  assert(*currentContext != NULL);

  EPUB3Error error = kEPUB3Success;
  const xmlChar *name = xmlTextReaderConstLocalName(reader);
  xmlReaderTypes currentNodeType = xmlTextReaderNodeType(reader);

  if(name != NULL && currentNodeType != XML_READER_TYPE_COMMENT) {
    switch((*currentContext)->state)
    {
      case kEPUB3NCXStateRoot:
      {
        //LOGE("NCX ROOT: %s\n", name);
        if(currentNodeType == XML_READER_TYPE_ELEMENT) {
          if(xmlStrcmp(name, BAD_CAST "navMap") == 0) {
            (void)EPUB3SaveParseContext(currentContext, kEPUB3NCXStateNavMap, name, 0, NULL, kEPUB3_YES, NULL);
          }
        }
        break;
      }
      case kEPUB3NCXStateNavMap:
      {

        //LOGE("NCX NAV MAP: %s\n", name);
        if(currentNodeType == XML_READER_TYPE_END_ELEMENT && xmlStrcmp(name, BAD_CAST "navMap") == 0) {
          (void)EPUB3PopAndFreeParseContext(currentContext);
            return kEPUB3NCXNavMapEnd;
        } else {
          error = EPUB3ProcessXMLReaderNodeForNavMapInNCX(epub, reader, currentContext);
        }
        break;
      }
      default: break;
    }
  }
  return error;
}

// TODO 不支持嵌套目录
EPUB3Error EPUB3ProcessXMLReaderNodeForNavMapInNCX(EPUB3Ref epub, xmlTextReaderPtr reader, EPUB3XMLParseContextPtr *context)
{
  assert(epub != NULL);
  assert(reader != NULL);
  EPUB3Error error = kEPUB3Success;
  const xmlChar *name = xmlTextReaderConstLocalName(reader);
  xmlReaderTypes nodeType = xmlTextReaderNodeType(reader);

  switch(nodeType)
  {
    case XML_READER_TYPE_ELEMENT:
    {
        if(xmlStrcmp(name, BAD_CAST "navPoint") == 0) {
          EPUB3TocItemRef newTocItem = EPUB3TocItemCreate();
          (void)EPUB3SaveParseContext(context, kEPUB3NCXStateNavMap, name, 0, NULL, kEPUB3_NO, newTocItem);
        }
        else if(xmlStrcmp(name, BAD_CAST "text") == 0 && xmlStrcmp((*context)->tagName, BAD_CAST "navLabel") == 0) {
          void * userInfo = (*context)->userInfo;
          (void)EPUB3SaveParseContext(context, kEPUB3NCXStateNavMap, name, 0, NULL, kEPUB3_YES, userInfo);
        }
        else if(xmlStrcmp(name, BAD_CAST "content") == 0) {
            void * userInfo = (*context)->userInfo;
            (void)EPUB3SaveParseContext(context, kEPUB3NCXStateNavMap, name, 0, NULL, kEPUB3_NO, userInfo);
            const xmlChar *value = xmlTextReaderGetAttribute(reader, BAD_CAST "src");
            if(value != NULL) {
                EPUB3TocItemRef tocItem = (EPUB3TocItemRef)userInfo;
                if(tocItem != NULL) {
                    tocItem->href = strdup((const char *)value);
                }
                EPUB3TocAddRootItem(epub->toc, tocItem);
                EPUB3TocItemRelease(tocItem);
                (void) EPUB3PopAndFreeParseContext(context);
            }
        }
        else if(!xmlTextReaderIsEmptyElement(reader)) {
          void * userInfo = (*context)->userInfo;
          (void)EPUB3SaveParseContext(context, kEPUB3NCXStateNavMap, name, 0, NULL, kEPUB3_NO, userInfo);
        }
      break;
    }
    case XML_READER_TYPE_TEXT:
    {
      if((*context)->shouldParseTextNode) {
        const xmlChar *value = xmlTextReaderValue(reader);
        if(value != NULL) {
          if(xmlStrcmp((*context)->tagName, BAD_CAST "text") == 0) {
            EPUB3TocItemRef tocItem = (EPUB3TocItemRef) (*context)->userInfo;
            if(tocItem != NULL) {
              tocItem->title = strdup((const char *)value);
                //LOGE("XML_READER_TYPE_TEXT: %s", value);
            }
          }
        }
      }
      break;
    }
//    case XML_READER_TYPE_END_ELEMENT:
//    {
//      if(xmlStrcmp(name, BAD_CAST "navPoint") == 0) {
//        if((*context)->userInfo != NULL) {
//          EPUB3TocItemRef newTocItem = (*context)->userInfo;
//          EPUB3TocAddRootItem(epub->toc, newTocItem);
//          EPUB3TocItemRelease(newTocItem);
//        }
//      }
//      (void)EPUB3PopAndFreeParseContext(context);
//      break;
//    }
    default: break;
  }
  return error;
}


#pragma mark - Validation

EPUB3Error EPUB3ValidateMimetype(EPUB3Ref epub)
{
  assert(epub != NULL);

  if(epub->archive == NULL) return kEPUB3ArchiveUnavailableError;

  EPUB3Error status = kEPUB3InvalidMimetypeError;
  static const char * requiredMimetype = "application/epub+zip";
  static const int stringLength = 20;
  char buffer[stringLength];

  if(unzGoToFirstFile(epub->archive) == UNZ_OK) {
    uint32_t stringLength = (uint32_t)strlen(requiredMimetype);
    if(unzOpenCurrentFile(epub->archive) == UNZ_OK) {
      int byteCount = unzReadCurrentFile(epub->archive, buffer, stringLength);
      if(byteCount == stringLength) {
        if(strncmp(requiredMimetype, buffer, stringLength) == 0) {
          status = kEPUB3Success;
        }
      }
    }
    unzCloseCurrentFile(epub->archive);
  }
  return status;
}

EXPORT EPUB3Error EPUB3CopyRootFilePathFromContainer(EPUB3Ref epub, char ** rootPath)
{
  assert(epub != NULL);

  if(epub->archive == NULL) return kEPUB3ArchiveUnavailableError;

  static const char *containerFilename = "META-INF/container.xml";

  void *buffer = NULL;
  uint32_t bufferSize = 0;
  uint32_t bytesCopied;

  xmlTextReaderPtr reader = NULL;
  EPUB3Bool foundPath = kEPUB3_NO;

  EPUB3Error error = kEPUB3Success;

  error = EPUB3CopyFileIntoBuffer(epub, &buffer, &bufferSize, &bytesCopied, containerFilename);
  if(error == kEPUB3Success) {
    reader = xmlReaderForMemory(buffer, bufferSize, "", NULL, XML_PARSE_RECOVER);
    if(reader != NULL) {
      int retVal;
      while((retVal = xmlTextReaderRead(reader)) == 1)
      {
        const char *rootFileName = "rootfile";
        const xmlChar *name = xmlTextReaderConstLocalName(reader);

        if(xmlTextReaderNodeType(reader) == XML_READER_TYPE_ELEMENT && xmlStrcmp(name, BAD_CAST rootFileName) == 0) {
          xmlChar *fullPath = xmlTextReaderGetAttribute(reader, BAD_CAST "full-path");
          if(fullPath != NULL) {
            // TODD: validate that the full-path attribute is of the form path-rootless
            //       see http://idpf.org/epub/30/spec/epub30-ocf.html#sec-container-metainf-container.xml
            foundPath = kEPUB3_YES;
            *rootPath = strdup((char *)fullPath);
          } else {
            // The spec requires the full-path attribute
            error = kEPUB3XMLXDocumentInvalidError;
          }
          break;
        }
      }
      if(retVal < 0) {
        error = kEPUB3XMLParseError;
      }
      if(!foundPath) {
        error = kEPUB3XMLXElementNotFoundError;
      }
    } else {
      error = kEPUB3XMLReadFromBufferError;
    }
    EPUB3_FREE_AND_NULL(buffer);
  }
  xmlFreeTextReader(reader);
  return error;
}

EPUB3Error EPUB3ValidateFileExistsAndSeekInArchive(EPUB3Ref epub, const char * filename)
{
  assert(epub != NULL);
  assert(filename != NULL);

  if(epub->archive == NULL) return kEPUB3ArchiveUnavailableError;

  EPUB3Error error = kEPUB3FileNotFoundInArchiveError;
  if(unzLocateFile(epub->archive, filename, 1) == UNZ_OK) {
    error = kEPUB3Success;
  }
  return error;
}

#pragma mark - Utility functions

// This buffer size was chosen because it matches UNZ_BUFSIZE in unzip.c
#define FILE_EXTRACT_BUFFER_SIZE (16384)

EXPORT EPUB3Error EPUB3ExtractArchiveToPath(EPUB3Ref epub, const char * path)
{
  assert(epub != NULL);
  assert(path != NULL);

  EPUB3Error error = kEPUB3UnknownError;

  char cwd[MAXNAMLEN];
  (void)getcwd(cwd, MAXNAMLEN);

  EPUB3Bool directoryReady = (chdir(path) >= 0);

  if(!directoryReady) {
    if(errno == ENOENT) {
      if(mkdir(path, 0755) < 0) {
        fprintf(stderr, "Error [%d] creating directory %s\n", errno, path);
        error = kEPUB3UnknownError;
      } else {
        if(chdir(path) < 0) {
          fprintf(stderr, "Error [%d] changing to newly created dir %s\n", errno, path);
        } else {
          directoryReady = kEPUB3_YES;
        }
      }
    } else {
      fprintf(stderr, "Error [%d] opening %s\n", errno, path);
      error = kEPUB3UnknownError;
    }
  }

  if(directoryReady) {
    if(unzGoToFirstFile(epub->archive) == UNZ_OK) {
      int fileCount = 0;
      do {
        error = EPUB3WriteCurrentArchiveFileToPath(epub, path);
        if(error == kEPUB3Success) {
          fileCount++;
        }
      } while(unzGoToNextFile(epub->archive) == UNZ_OK);
      if(fileCount == EPUB3GetFileCountInArchive(epub)) {
        error = kEPUB3Success;
      }
    }
  }

  if(chdir(cwd) < 0) {
    fprintf(stderr, "Error [%d] changing back to starting dir %s\n", errno, cwd);
  }
  return error;
}

EPUB3Error EPUB3CreateNestedDirectoriesForFileAtPath(const char * path)
{
  EPUB3Error error = kEPUB3Success;
  char * pathCopy = strdup(path);
  char pathBuildup[strlen(path) + 1];
  pathBuildup[0] = '\0';
  char * pathseg;
  char * pathseg2;
  char * loc;

  pathseg = strtok_r(pathCopy, "/", &loc);

  while(pathseg != NULL) {
    pathseg2 = strtok_r(NULL, "/", &loc);
    if(pathseg2 != NULL) {
      strncat(pathBuildup, "/", 1U);
      strncat(pathBuildup, pathseg, strlen(pathseg));
      struct stat st;
      if(stat(pathBuildup, &st) < 0) {
        if(errno == ENOENT) {
          //Directory doesn't exist
          if(mkdir(pathBuildup, 0755) >= 0) {
            error = kEPUB3Success;
          } else {
            // Couldn't create dir
            error = kEPUB3UnknownError;
            break;
          }
        } else {
          // Weird stat error
          error = kEPUB3UnknownError;
          break;
        }
      } else {
        // Already exists
        error = kEPUB3Success;
      }
    }
    pathseg = pathseg2;
  }
  EPUB3_FREE_AND_NULL(pathCopy);
  return error;
}

EPUB3Error EPUB3WriteCurrentArchiveFileToPath(EPUB3Ref epub, const char * path)
{
  EPUB3Error error = kEPUB3Success;
  unz_file_info fileInfo;
  char filename[MAXNAMLEN];
  if(unzGetCurrentFileInfo(epub->archive, &fileInfo, filename, MAXNAMLEN, NULL, 0, NULL, 0) == UNZ_OK) {
    uLong pathlen = strlen(path) + 1U + strlen(filename) + 1U;
    char fullpath[pathlen];
    (void)strcpy(fullpath, path);
    (void)strncat(fullpath, "/", 1U);
    (void)strncat(fullpath, filename, strlen(filename));

    FILE *destination = fopen(fullpath, "wb");
    if(destination == NULL) {
      if(errno == ENOENT) {
        //We need to create intermediate directories
        error = EPUB3CreateNestedDirectoriesForFileAtPath(fullpath);
        if(error == kEPUB3Success) {
          //Try again
          if((destination = fopen(fullpath, "wb")) == NULL) {
            //Failed again can't continue
            error = kEPUB3UnknownError;
          }
        }
      } else {
        error = kEPUB3UnknownError;
      }
    }
    if(destination != NULL) {
      void *buffer = malloc(FILE_EXTRACT_BUFFER_SIZE);
      if(unzOpenCurrentFile(epub->archive) == UNZ_OK) {
        int bytesRead;
        do {
          bytesRead = unzReadCurrentFile(epub->archive, buffer, FILE_EXTRACT_BUFFER_SIZE);
          if(bytesRead < 0) {
            error = kEPUB3FileReadFromArchiveError;
            break;
          } else {
            fwrite(buffer, 1, bytesRead, destination);
          }
        } while(bytesRead > 0);
        unzCloseCurrentFile(epub->archive);
      }
      fclose(destination);
      EPUB3_FREE_AND_NULL(buffer);
    }
  }
  return error;
}

EXPORT EPUB3Error EPUB3CopyFileIntoBuffer(EPUB3Ref epub, void **buffer, uint32_t *bufferSize, uint32_t *bytesCopied, const char * filename)
{
  assert(epub != NULL);
  assert(filename != NULL);
  assert(buffer != NULL);

  if(epub->archive == NULL) return kEPUB3ArchiveUnavailableError;

  EPUB3Error error = kEPUB3InvalidArgumentError;
  if(filename != NULL) {
    uint32_t bufSize = 1;
    error = EPUB3GetUncompressedSizeOfFileInArchive(epub, &bufSize, filename);
    if(error == kEPUB3Success) {
      if(unzOpenCurrentFile(epub->archive) == UNZ_OK) {
        *buffer = calloc(bufSize, sizeof(char));
        int32_t copied = unzReadCurrentFile(epub->archive, *buffer, bufSize);
        if(copied >= 0) {
          if(bytesCopied != NULL) {
            *bytesCopied = copied;
          }
          if(bufferSize != NULL) {
            *bufferSize = bufSize;
          }
          error = kEPUB3Success;
        } else {
          free(*buffer);
          *buffer = NULL;
          error = kEPUB3FileReadFromArchiveError;
        }
      }
    }
  }
  return error;
}

EPUB3Error EPUB3GetUncompressedSizeOfFileInArchive(EPUB3Ref epub, uint32_t *uncompressedSize, const char *filename)
{
  assert(epub != NULL);
  assert(filename != NULL);
  assert(uncompressedSize != NULL);

  if(epub->archive == NULL) return kEPUB3ArchiveUnavailableError;

  EPUB3Error error = EPUB3ValidateFileExistsAndSeekInArchive(epub, filename);
  if(error == kEPUB3Success) {
    unz_file_info fileInfo;
    if(unzGetCurrentFileInfo(epub->archive, &fileInfo, NULL, 0, NULL, 0, NULL, 0) == UNZ_OK) {
      *uncompressedSize = (uint32_t)fileInfo.uncompressed_size;
      error = kEPUB3Success;
    }
  }
  return error;
}

uint32_t EPUB3GetFileCountInArchive(EPUB3Ref epub)
{
  unz_global_info gi;
	int err = unzGetGlobalInfo(epub->archive, &gi);
	if (err != UNZ_OK)
    return err;

	return (uint32_t)gi.number_entry;
}

EXPORT char * EPUB3CopyOfPathByDeletingLastPathComponent(const char * path)
{
  assert(path != NULL);

  char * pathCopy = strdup(path);
  char pathBuildup[strlen(path) + 1];
  pathBuildup[0] = '\0';
  char * pathseg;
  char * pathseg2;
  char * loc;

  pathseg = strtok_r(pathCopy, "/", &loc);

  while(pathseg != NULL) {
    pathseg2 = strtok_r(NULL, "/", &loc);
    if(pathseg2 != NULL) {
      strncat(pathBuildup, pathseg, strlen(pathseg));
      strncat(pathBuildup, "/", 1U);
    }
    pathseg = pathseg2;
  }
  EPUB3_FREE_AND_NULL(pathCopy);

  return strdup(pathBuildup);
}

EXPORT char * EPUB3CopyOfPathByAppendingPathComponent(const char * path, const char * componentToAppend)
{
  assert(path != NULL);
  assert(componentToAppend != NULL);

  uLong basePathLen = strlen(path);

  EPUB3Bool shouldAddSeparator = kEPUB3_NO;

  if(basePathLen > 0 && path[basePathLen - 1] != '/') {
    shouldAddSeparator = kEPUB3_YES;
    basePathLen++;
  }

  uLong pathlen = basePathLen + strlen(componentToAppend) + 1U;
  char fullpath[pathlen];
  (void)strcpy(fullpath, path);
  if(shouldAddSeparator) {
    (void)strncat(fullpath, "/", 1U);
  }
  (void)strncat(fullpath, componentToAppend, strlen(componentToAppend));
  return strdup(fullpath);
}

/* high-level cover getter */
static const char* get_image_extension(const unsigned char* bytes, size_t size) {
    if (size >= 4) {
        if (bytes[0] == 0xFF && bytes[1] == 0xD8) return "jpg";
        if (memcmp(bytes, "\x89PNG", 4) == 0) return "png";
        if (memcmp(bytes, "GIF8", 4) == 0) return "gif";
        if (memcmp(bytes, "\x49\x49\x2A\x00", 4) == 0) return "tif";
        if (memcmp(bytes, "\x42\x4D", 2) == 0) return "bmp";
    }
    return "dat";
}

int SaveEPUBCoverSameName(const char* epubPath) {
    if (!epubPath) return -1;  // Generic error code

    EPUB3Error result;
    EPUB3Ref epub = NULL;
    void *coverBytes = NULL;
    uint32_t coverSize = 0;
    FILE *outputFile = NULL;
    char outputPath[1024];

    // Extract base filename without extension
    char *base = strdup(epubPath);
    char *filename = basename(base);
    char *dot = strrchr(filename, '.');
    if (dot) *dot = '\0';

    // Get cover image
    epub = EPUB3CreateWithArchiveAtPath(epubPath, &result);
    if (!epub) goto cleanup;

    result = EPUB3CopyCoverImage(epub, &coverBytes, &coverSize);
    if (result != kEPUB3Success || !coverBytes || !coverSize) {
        goto cleanup;
    }

    // Build output path
    const char *ext = get_image_extension(coverBytes, coverSize);
    snprintf(outputPath, sizeof(outputPath), "%s.%s", filename, ext);

    // Write file
    outputFile = fopen(outputPath, "wb");
    if (!outputFile) {
        goto cleanup;
    }

    fwrite(coverBytes, 1, coverSize, outputFile);
    printf("Saved cover to: %s\n", outputPath);

cleanup:
    if (outputFile) fclose(outputFile);
    if (coverBytes) free(coverBytes);
    if (epub) EPUB3Release(epub);
    free(base);
    return (result == kEPUB3Success) ? 0 : 1;
}

EXPORT int SaveEPUBCoverToDir(const char *epubPath, const char *outputDir) {
    EPUB3Error result;
    EPUB3Ref epub = NULL;
    void *coverBytes = NULL;
    uint32_t coverSize = 0;
    FILE *outputFile = NULL;
    char outputPath[1024];
    char *baseName = NULL;
    char *baseCopy = NULL;

    // Create copy for basename manipulation
    baseCopy = strdup(epubPath);
    baseName = basename(baseCopy);

    // Trim .epub extension
    char *dot = strrchr(baseName, '.');
    if (dot && !strcasecmp(dot, ".epub")) *dot = '\0';

    // Get cover image
    epub = EPUB3CreateWithArchiveAtPath(epubPath, &result);
    if (!epub) goto cleanup;

    result = EPUB3CopyCoverImage(epub, &coverBytes, &coverSize);
    if (result != kEPUB3Success || !coverBytes || !coverSize) goto cleanup;

    // Build output path
    const char *ext = get_image_extension(coverBytes, coverSize);
    snprintf(outputPath, sizeof(outputPath), "%s/%s.%s",
             outputDir, baseName, ext);

    // Write file
    outputFile = fopen(outputPath, "wb");
    if (!outputFile) goto cleanup;

    fwrite(coverBytes, 1, coverSize, outputFile);

cleanup:
    if (outputFile) fclose(outputFile);
    if (coverBytes) free(coverBytes);
    if (epub) EPUB3Release(epub);
    free(baseCopy);
    return (result == kEPUB3Success) ? 0 : 1;
}