//
//  BNObject.m
//  BitnashKit
//
//  Created by Rich Collins on 4/16/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNObject.h"
#import "NSObject+BNJSON.h"

@implementation BNObject

- (id)init
{
    self = [super init];
    self.serializedSlotNames = [NSMutableArray array];
    return self;
}

- (id)asJSONObject
{
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    for (NSString *property in self.serializedSlotNames)
    {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
        id object = [self performSelector:NSSelectorFromString(property)];
#pragma clang diagnostic pop
        if (object == nil)
        {
            object = [NSNull null];
        }
        [dict setObject:[object asJSONObject] forKey:property];
    }
    [dict setObject:NSStringFromClass([self class])  forKey:@"type"];
    return dict;
}

- (id)asObjectFromJSONObject
{
    return self;
}

- (void)awakeFromJSONDict:(NSDictionary *)dict
{
    for (NSString *propertyName in self.serializedSlotNames)
    {
        NSMutableString *setterName = [NSMutableString stringWithString:@"set"];
        [setterName appendString:[[propertyName substringToIndex:1] capitalisedFirstCharacterString]];
        if ([propertyName length] > 1)
        {
            [setterName appendString:[propertyName substringFromIndex:1]];
        }
        [setterName appendString:@":"];
        //NSLog(@"%@", propertyName);
        //NSLog(@"%@", setterName);
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
        if ([self respondsToSelector:NSSelectorFromString(setterName)])
        {
            id obj = [[dict objectForKey:propertyName] asObjectFromJSONObject];
            
            if ([obj respondsToSelector:@selector(setBnParent:)])
            {
                [obj performSelector:@selector(setBnParent:) withObject:self];
            }
            [self performSelector:NSSelectorFromString(setterName) withObject:obj];
        }
#pragma clang diagnostic pop
    }
    
}

- (void)writeToFile:(NSString *)filePath
{
    [[self asJSONString] writeToFile:filePath atomically:YES encoding:NSUTF8StringEncoding error:nil];
}

//TODO better implementation
- (void)copySlotsFrom:(BNObject *)other
{
    [self awakeFromJSONDict:[other asJSONObject]];
}

@end
