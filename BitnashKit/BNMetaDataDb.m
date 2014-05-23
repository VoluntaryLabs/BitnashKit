//
//  BNMetaDataDb.m
//  BitnashKit
//
//  Created by Rich Collins on 5/23/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNMetaDataDb.h"

@implementation BNMetaDataDb

static BNMetaDataDb *shared = nil;

+ (BNMetaDataDb *)shared
{
    if (!shared)
    {
        shared = [[BNMetaDataDb alloc] init];
    }
    
    return shared;
}

@synthesize path = _path;

- (NSString *)path
{
    return _path;
}

- (void)setPath:(NSString *)path
{
    _path = path;
    
    NSError *error;
    [[NSFileManager defaultManager] createDirectoryAtPath:path
                              withIntermediateDirectories:YES
                                               attributes:nil
                                                    error:&error];
    
    if (error)
    {
        [NSException raise:error.localizedDescription format:nil];
    }
}

- (NSString *)metaDataPathFor:(BNObject *)bnObject
{
    return [self.path stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.%lx", bnObject.className, (unsigned long)bnObject.hash]];
}

- (void)readToBnObject:(BNObject *)bnObject
{
    NSError *error;
    
    NSData *data = [NSData dataWithContentsOfFile:[self metaDataPathFor:bnObject]];
    bnObject.metaData = [NSJSONSerialization JSONObjectWithData:data options:0x0 error:&error];
    
    if (error)
    {
        [NSException raise:error.localizedDescription format:nil];
    }
}

- (void)writeFromBnObject:(BNObject *)bnObject
{
    NSError *error;
    
    NSData *data = [NSJSONSerialization dataWithJSONObject:bnObject.metaData options:0x0 error:&error];
    
    if (error)
    {
        [NSException raise:error.localizedDescription format:nil];
    }
    
    [data writeToFile:[self metaDataPathFor:bnObject] atomically:YES];
}

@end